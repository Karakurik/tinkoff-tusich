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

package object achievement {

  @derive(loggable, encoder, decoder)
  @newtype
  case class AchievementId(value: Long)
  object AchievementId {
    implicit val doobieRead: Read[AchievementId] = Read[Long].map(AchievementId(_))
    implicit val schema: Schema[AchievementId] =
      Schema.schemaForLong.map(l => Some(AchievementId(l)))(_.value)
    implicit val codec: Codec[String, AchievementId, TextPlain] =
      Codec.long.map(AchievementId(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class AchievementName(value: String)
  object AchievementName {
    implicit val doobieRead: Read[AchievementName] = Read[String].map(AchievementName(_))
    implicit val schema: Schema[AchievementName] =
      Schema.schemaForString.map(n => Some(AchievementName(n)))(_.value)
    implicit val codec: Codec[String, AchievementName, TextPlain] =
      Codec.string.map(AchievementName(_))(_.value)
  }
}
