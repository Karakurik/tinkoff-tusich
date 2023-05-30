import cats.data.ReaderT
import cats.effect.IO
import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.util.Read
import io.estatico.newtype.macros.newtype
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, Schema}
import tofu.logging.derivation._

import java.time.Instant

package object domain {

  @derive(loggable, encoder, decoder)
  @newtype
  case class TodoId(value: Long)
  object TodoId {
    implicit val doobieRead: Read[TodoId] = Read[Long].map(TodoId(_))
    implicit val schema: Schema[TodoId] =
      Schema.schemaForLong.map(l => Some(TodoId(l)))(_.value)
    implicit val codec: Codec[String, TodoId, TextPlain] =
      Codec.long.map(TodoId(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class TodoName(value: String)
  object TodoName {
    implicit val doobieRead: Read[TodoName] = Read[String].map(TodoName(_))
    implicit val schema: Schema[TodoName] =
      Schema.schemaForString.map(n => Some(TodoName(n)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class RemainderDate(value: Instant)
  object RemainderDate {
    implicit val doobieRead: Read[RemainderDate] =
      Read[Long].map(ts => RemainderDate(Instant.ofEpochMilli(ts)))
    implicit val schema: Schema[RemainderDate] = Schema.schemaForString.map(n =>
      Some(RemainderDate(Instant.parse(n)))
    )(_.value.toString)
  }

  type IOWithRequestContext[A] = ReaderT[IO, RequestContext, A]
}
