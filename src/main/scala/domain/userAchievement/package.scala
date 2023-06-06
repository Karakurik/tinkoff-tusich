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

package object userAchievement {

  @derive(loggable, encoder, decoder)
  @newtype
  case class UserAchievementId(value: Long)
  object UserAchievementId {
    implicit val doobieRead: Read[UserAchievementId] = Read[Long].map(UserAchievementId(_))
    implicit val schema: Schema[UserAchievementId] =
      Schema.schemaForLong.map(l => Some(UserAchievementId(l)))(_.value)
    implicit val codec: Codec[String, UserAchievementId, TextPlain] =
      Codec.long.map(UserAchievementId(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class UserAchievementLevel(value: Long)
  object UserAchievementLevel {
    implicit val doobieRead: Read[UserAchievementLevel] = Read[Long].map(UserAchievementLevel(_))
    implicit val schema: Schema[UserAchievementLevel] =
      Schema.schemaForLong.map(n => Some(UserAchievementLevel(n)))(_.value)
    implicit val codec: Codec[String, UserAchievementLevel, TextPlain] =
      Codec.long.map(UserAchievementLevel(_))(_.value)
  }
}
