package domain.user

import domain.errors.AppError

object errors {
  case class UserAlreadyExists()
    extends AppError("User with same firstName and lastName already exists")

  case class UserNotFoundById(id: UserId)
    extends AppError(s"User with id ${id.value} not found")

  case class UserNotFoundByFirstNameAndLAstName(firstName: UserFirstName, lastName: UserLastName)
    extends AppError(s"User with firstName ${firstName.value} and lastName ${lastName.value} not found")
}
