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

package object participation {

  @derive(loggable, encoder, decoder)
  @newtype
  case class ParticipationId(value: Long)
  object ParticipationId {
    implicit val doobieRead: Read[ParticipationId] = Read[Long].map(ParticipationId(_))
    implicit val schema: Schema[ParticipationId] =
      Schema.schemaForLong.map(l => Some(ParticipationId(l)))(_.value)
    implicit val codec: Codec[String, ParticipationId, TextPlain] =
      Codec.long.map(ParticipationId(_))(_.value)
  }
}
