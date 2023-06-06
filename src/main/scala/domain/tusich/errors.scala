package domain.tusich

import domain.errors.AppError

object errors {
  case class TusichAlreadyExists()
    extends AppError("Tusich with same name already exists")

  case class TusichNotFoundById(id: TusichId)
    extends AppError(s"Tusich with id ${id.value} not found")

  case class TusichNotFoundByName(name: TusichName)
    extends AppError(s"Tusich with name ${name.value} not found")
}
