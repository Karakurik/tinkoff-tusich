package domain.user

import derevo.circe.{decoder, encoder}
import derevo.derive
import domain.achievement.Achievement
import domain.userAchievement.UserAchievement
import sttp.tapir.Schema
import tofu.logging.derivation._

@derive(loggable, encoder, decoder)
final case class CreateUser(firstName: UserFirstName, lastName: UserLastName)

@derive(loggable, encoder, decoder)
final case class ReadUser(id: UserId, firstName: UserFirstName, lastName: UserLastName)

@derive(loggable, encoder, decoder)
final case class User(id: UserId, firstName: UserFirstName, lastName: UserLastName, achievements: List[UserAchievement])

object User {
  implicit val schema: Schema[User] = Schema.derived
}
