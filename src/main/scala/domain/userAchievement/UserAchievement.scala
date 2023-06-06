package domain.userAchievement

import derevo.circe.{decoder, encoder}
import derevo.derive
import domain.achievement.{Achievement, AchievementId}
import domain.user.UserId
import sttp.tapir.Schema
import tofu.logging.derivation._

@derive(loggable, encoder, decoder)
final case class CreateUserAchievement(userId: UserId, achievementId: AchievementId)

@derive(loggable, encoder, decoder)
final case class ReadUserAchievement(id: UserAchievementId, userId: UserId, achievementId: AchievementId, level: UserAchievementLevel)

@derive(loggable, encoder, decoder)
final case class UserAchievement(id: UserAchievementId, userId: UserId, achievement: Achievement, level: UserAchievementLevel)

object UserAchievement {
  implicit val schema: Schema[UserAchievement] = Schema.derived
}
