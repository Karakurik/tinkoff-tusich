package domain.achievement

import domain.errors.AppError

object errors {
  case class AchievementAlreadyExists()
    extends AppError("Achievement with same name already exists")

  case class AchievementNotFoundById(id: AchievementId)
    extends AppError(s"Achievement with id ${id.value} not found")

  case class AchievementNotFoundByName(name: AchievementName)
    extends AppError(s"Achievement with name ${name.value} not found")
}
