package service.user

import cats.syntax.applicativeError._
import cats.syntax.either._
import dao.user.UserSql
import domain._
import domain.errors._
import domain.user._
import domain.userAchievement.{CreateUserAchievement, ReadUserAchievement, UserAchievement}
import doobie._
import doobie.implicits._
import tofu.logging.Logging

trait UserStorage {
  def list: IOWithRequestContext[Either[InternalError, List[User]]]

  def findById(
                id: UserId
              ): IOWithRequestContext[Either[InternalError, Option[User]]]

  def findByFirstNameAndLastName(
                                  firstName: UserFirstName,
                                  lastName: UserLastName
                                ): IOWithRequestContext[Either[InternalError, Option[User]]]

  def removeById(id: UserId): IOWithRequestContext[Either[AppError, Unit]]

  def create(user: CreateUser): IOWithRequestContext[Either[AppError, User]]

  def createUserAchievement(userId: UserId, createUserAchievement: CreateUserAchievement): IOWithRequestContext[Either[InternalError, Option[ReadUserAchievement]]]
}

object UserStorage {
  def make(
            sql: UserSql,
            transactor: Transactor[IOWithRequestContext]
          ): UserStorage = {
    implicit val logs =
      Logging.Make
        .contextual[IOWithRequestContext, RequestContext]
        .forService[UserStorage]
    val storage = new Impl(sql, transactor)
    new LoggingImpl(storage)
  }

  private final class Impl(
                            sql: UserSql,
                            transactor: Transactor[IOWithRequestContext]
                          ) extends UserStorage {
    override def list: IOWithRequestContext[Either[InternalError, List[User]]] =
      sql.listAll.transact(transactor).attempt.map(_.leftMap(InternalError(_)))

    override def findById(
                           id: UserId
                         ): IOWithRequestContext[Either[InternalError, Option[User]]] = {
      sql
        .findById(id)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
    }

    override def findByFirstNameAndLastName(
                                             firstName: UserFirstName,
                                             lastName: UserLastName
                                           ): IOWithRequestContext[Either[InternalError, Option[User]]] = {
      sql
        .findByFirstNameAndLastName(firstName, lastName)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
    }

    override def removeById(
                             id: UserId
                           ): IOWithRequestContext[Either[AppError, Unit]] =
      sql.removeById(id).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Unit]
        case Right(Left(error)) => error.asLeft[Unit]
        case _ => ().asRight[AppError]
      }

    override def create(
                         user: CreateUser
                       ): IOWithRequestContext[Either[AppError, User]] =
      sql.create(user).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[User]
        case Right(Left(error)) => error.asLeft[User]
        case Right(Right(user)) => user.asRight[AppError]
      }

    override def createUserAchievement(
                                        userId: UserId,
                                        createUserAchievement: CreateUserAchievement
                                      ): IOWithRequestContext[Either[InternalError, Option[ReadUserAchievement]]] =
      sql
        .createUserAchievement(createUserAchievement)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
  }

  private final class LoggingImpl(storage: UserStorage)(implicit
                                                        logging: Logging[IOWithRequestContext]
  ) extends UserStorage {

    override def list: IOWithRequestContext[Either[InternalError, List[User]]] =
      surroundWithLogs[InternalError, List[User]]("Getting all users") {
        error =>
          (s"Error while getting all users: ${error.message}", error.cause)
      } { result =>
        s"All users: ${result.mkString}"
      }(storage.list)

    override def findById(
                           id: UserId
                         ): IOWithRequestContext[Either[InternalError, Option[User]]] =
      surroundWithLogs[InternalError, Option[User]](
        s"Getting user by id ${id.value}"
      ) { error =>
        (s"Error while getting user: ${error.message}\n", error.cause)
      } { result =>
        s"Found user: ${result.toString}"
      }(storage.findById(id))

    override def findByFirstNameAndLastName(
                                             firstName: UserFirstName,
                                             lastName: UserLastName
                                           ): IOWithRequestContext[Either[InternalError, Option[User]]] =
      surroundWithLogs[InternalError, Option[User]](
        s"Getting user by firstName ${firstName.value} and lastName ${lastName.value}"
      ) { error =>
        (s"Error while getting user: ${error.message}\n", error.cause)
      } { result =>
        s"Found user: ${result.toString}"
      }(storage.findByFirstNameAndLastName(firstName, lastName))

    override def removeById(
                             id: UserId
                           ): IOWithRequestContext[Either[AppError, Unit]] =
      surroundWithLogs[AppError, Unit](s"Removing user by id ${id.value}") {
        error => (s"Error while removing user: ${error.message}", error.cause)
      } { _ =>
        s"Removed user with id ${id.value}"
      }(storage.removeById(id))

    override def create(
                         user: CreateUser
                       ): IOWithRequestContext[Either[AppError, User]] =
      surroundWithLogs[AppError, User](s"Creating user with params $user") {
        error => (s"Error while creating user: ${error.message}", error.cause)
      } { user =>
        s"Created user $user"
      }(storage.create(user))
    override def createUserAchievement(
                                        userId: UserId,
                                        createUserAchievement: CreateUserAchievement
                                      ): IOWithRequestContext[Either[InternalError, Option[ReadUserAchievement]]] =
      surroundWithLogs[InternalError, Option[ReadUserAchievement]](
        s"Creating UserAchievement by userId ${userId}, achievementsId ${createUserAchievement.achievementId}"
      ) { error =>
        (s"Error while creating UserAchievement: ${error.message}\n", error.cause)
      } { result =>
        s"Created UserAchievement ${result.toString}"
      }(storage.createUserAchievement(userId, createUserAchievement))

    private def surroundWithLogs[Error, Res](
      inputLog: String
    )(errorOutputLog: Error => (String, Option[Throwable]))(
      successOutputLog: Res => String
    )(
      io: IOWithRequestContext[Either[Error, Res]]
    ): IOWithRequestContext[Either[Error, Res]] =
      for {
        _ <- logging.info(inputLog)
        res <- io
        _ <- res match {
          case Left(error) => {
            val (msg, cause) = errorOutputLog(error)
              cause.fold(logging.error(msg))(cause => logging.error(msg, cause))
          }
          case Right(result) => logging.info(successOutputLog(result))
        }

      } yield res

  }
}
