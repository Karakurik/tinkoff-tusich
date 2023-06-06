package controller.user

import domain._
import domain.errors._
import domain.user.{CreateUser, User, UserFirstName, UserId, UserLastName}
import domain.userAchievement.{CreateUserAchievement, ReadUserAchievement, UserAchievement}
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object endpoints {
  val listUser: PublicEndpoint[RequestContext, AppError, List[User], Any] =
    endpoint.get
      .in("user")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[User]])

  val findUserById
      : PublicEndpoint[(UserId, RequestContext), AppError, Option[User], Any] =
    endpoint.get
      .in("user")
      .in(path[UserId]("user-id"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[User]])

  val findUserByFirstNameAndLastName
  : PublicEndpoint[(UserFirstName, UserLastName, RequestContext), AppError, Option[User], Any] =
    endpoint.get
      .in("user")
      .in(query[UserFirstName]("first_name"))
      .in(query[UserLastName]("last_name"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[User]])

  val removeUser
      : PublicEndpoint[(UserId, RequestContext), AppError, Unit, Any] =
    endpoint.delete
      .in("user" / path[UserId])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])

  val createUser
      : PublicEndpoint[(CreateUser, RequestContext), AppError, User, Any] =
    endpoint.post
      .in("user")
      .in(jsonBody[CreateUser])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[User])

  val createUserAchievement
  : PublicEndpoint[(UserId, CreateUserAchievement, RequestContext), AppError, Option[ReadUserAchievement], Any] =
    endpoint.post
      .in("user" / path[UserId]("user-id") / "achievement")
      .in(jsonBody[CreateUserAchievement])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[ReadUserAchievement]])
}
