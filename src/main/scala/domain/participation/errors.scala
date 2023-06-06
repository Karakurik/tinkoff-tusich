package domain.participation

import domain.errors.AppError

object errors {
  case class ParticipationAlreadyExists()
    extends AppError("Participation with same user and tusich already exists")

  case class ParticipationNotFoundById(id: ParticipationId)
    extends AppError(s"Participation with id ${id.value} not found")
}
