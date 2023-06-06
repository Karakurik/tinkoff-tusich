package domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.util.Read
import io.estatico.newtype.macros.newtype
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, Schema}
import tofu.logging.derivation._

package object user {

  @derive(loggable, encoder, decoder)
  @newtype
  case class UserId(value: Long)

  @derive(loggable, encoder, decoder)
  @newtype
  case class UserFirstName(value: String)

  @derive(loggable, encoder, decoder)
  @newtype
  case class UserLastName(value: String)

  object UserId {
    implicit val doobieRead: Read[UserId] = Read[Long].map(UserId(_))
    implicit val schema: Schema[UserId] =
      Schema.schemaForLong.map(l => Some(UserId(l)))(_.value)
    implicit val codec: Codec[String, UserId, TextPlain] =
      Codec.long.map(UserId(_))(_.value)
  }

  object UserFirstName {
    implicit val doobieRead: Read[UserFirstName] = Read[String].map(UserFirstName(_))
    implicit val schema: Schema[UserFirstName] =
      Schema.schemaForString.map(n => Some(UserFirstName(n)))(_.value)
    implicit val codec: Codec[String, UserFirstName, TextPlain] =
      Codec.string.map(UserFirstName(_))(_.value)
  }

  object UserLastName {
    implicit val doobieRead: Read[UserLastName] = Read[String].map(UserLastName(_))
    implicit val schema: Schema[UserLastName] =
      Schema.schemaForString.map(n => Some(UserLastName(n)))(_.value)
    implicit val codec: Codec[String, UserLastName, TextPlain] =
      Codec.string.map(UserLastName(_))(_.value)
  }
}
