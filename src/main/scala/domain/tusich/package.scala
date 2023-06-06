package domain

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

package object tusich {

  @derive(loggable, encoder, decoder)
  @newtype
  case class TusichId(value: Long)
  object TusichId {
    implicit val doobieRead: Read[TusichId] = Read[Long].map(TusichId(_))
    implicit val schema: Schema[TusichId] =
      Schema.schemaForLong.map(l => Some(TusichId(l)))(_.value)
    implicit val codec: Codec[String, TusichId, TextPlain] =
      Codec.long.map(TusichId(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class TusichName(value: String)
  object TusichName {
    implicit val doobieRead: Read[TusichName] = Read[String].map(TusichName(_))
    implicit val schema: Schema[TusichName] =
      Schema.schemaForString.map(n => Some(TusichName(n)))(_.value)
    implicit val codec: Codec[String, TusichName, TextPlain] =
      Codec.string.map(TusichName(_))(_.value)
  }
}
