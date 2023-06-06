package domain.userAchievement

import domain.achievement.{AchievementId, AchievementName}
import domain.errors.AppError

object errors {
  case class UserAchievementAlreadyExists()
    extends AppError("UserAchievement with same name already exists")

  case class UserAchievementNotFoundById(id: UserAchievementId)
    extends AppError(s"UserAchievement with id ${id.value} not found")

  case class UserAchievementNotFound()
    extends AppError(s"UserAchievement not found")
}
