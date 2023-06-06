package service.achievement

import cats.syntax.applicativeError._
import cats.syntax.either._
import dao.achievement.AchievementSql
import domain._
import domain.achievement._
import domain.errors._
import domain.tusich.TusichId
import doobie._
import doobie.implicits._
import tofu.logging.Logging

trait AchievementStorage {
  def list: IOWithRequestContext[Either[InternalError, List[Achievement]]]

  def listByTusichId(tusichId: TusichId): IOWithRequestContext[Either[InternalError, List[Achievement]]]

  def findById(
                id: AchievementId
              ): IOWithRequestContext[Either[InternalError, Option[Achievement]]]

  def findByName(
                  name: AchievementName
                ): IOWithRequestContext[Either[InternalError, Option[Achievement]]]

  def removeById(id: AchievementId): IOWithRequestContext[Either[AppError, Unit]]

  def create(achievement: CreateAchievement): IOWithRequestContext[Either[AppError, Achievement]]
}

object AchievementStorage {
  def make(
            sql: AchievementSql,
            transactor: Transactor[IOWithRequestContext]
          ): AchievementStorage = {
    implicit val logs =
      Logging.Make
        .contextual[IOWithRequestContext, RequestContext]
        .forService[AchievementStorage]
    val storage = new Impl(sql, transactor)
    new LoggingImpl(storage)
  }

  private final class Impl(
                            sql: AchievementSql,
                            transactor: Transactor[IOWithRequestContext]
                          ) extends AchievementStorage {
    override def list: IOWithRequestContext[Either[InternalError, List[Achievement]]] =
      sql.listAll.transact(transactor).attempt.map(_.leftMap(InternalError(_)))

    override def listByTusichId(tusichId: TusichId): IOWithRequestContext[Either[InternalError, List[Achievement]]] =
      sql.listAllByTusichId(tusichId).transact(transactor).attempt.map(_.leftMap(InternalError(_)))

    override def findById(
                           id: AchievementId
                         ): IOWithRequestContext[Either[InternalError, Option[Achievement]]] = {
      sql
        .findById(id)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
    }

    override def findByName(
                             name: AchievementName
                           ): IOWithRequestContext[Either[InternalError, Option[Achievement]]] = {
      sql
        .findByName(name)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
    }

    override def removeById(
                             id: AchievementId
                           ): IOWithRequestContext[Either[AppError, Unit]] =
      sql.removeById(id).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Unit]
        case Right(Left(error)) => error.asLeft[Unit]
        case _ => ().asRight[AppError]
      }

    override def create(
                         achievement: CreateAchievement
                       ): IOWithRequestContext[Either[AppError, Achievement]] =
      sql.create(achievement).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Achievement]
        case Right(Left(error)) => error.asLeft[Achievement]
        case Right(Right(achievement)) => achievement.asRight[AppError]
      }
  }

  private final class LoggingImpl(storage: AchievementStorage)(implicit
                                                               logging: Logging[IOWithRequestContext]
  ) extends AchievementStorage {

    override def list: IOWithRequestContext[Either[InternalError, List[Achievement]]] =
      surroundWithLogs[InternalError, List[Achievement]]("Getting all achievements") {
        error =>
          (s"Error while getting all achievements: ${error.message}", error.cause)
      } { result =>
        s"All achievements: ${result.mkString}"
      }(storage.list)

    override def listByTusichId(tusichId: TusichId): IOWithRequestContext[Either[InternalError, List[Achievement]]] =
      surroundWithLogs[InternalError, List[Achievement]](s"Getting all achievements by tusichId ${tusichId}") {
        error =>
          (s"Error while getting all achievements by tusichId ${tusichId}: ${error.message}", error.cause)
      } { result =>
        s"All achievements by tusichId ${tusichId}: ${result.mkString}"
      }(storage.list)

    override def findById(
                           id: AchievementId
                         ): IOWithRequestContext[Either[InternalError, Option[Achievement]]] =
      surroundWithLogs[InternalError, Option[Achievement]](
        s"Getting achievement by id ${id.value}"
      ) { error =>
        (s"Error while getting achievement: ${error.message}\n", error.cause)
      } { result =>
        s"Found achievement: ${result.toString}"
      }(storage.findById(id))

    override def findByName(
                           name: AchievementName
                         ): IOWithRequestContext[Either[InternalError, Option[Achievement]]] =
      surroundWithLogs[InternalError, Option[Achievement]](
        s"Getting achievement by name ${name.value}"
      ) { error =>
        (s"Error while getting achievement: ${error.message}\n", error.cause)
      } { result =>
        s"Found achievement: ${result.toString}"
      }(storage.findByName(name))

    override def removeById(
                             id: AchievementId
                           ): IOWithRequestContext[Either[AppError, Unit]] =
      surroundWithLogs[AppError, Unit](s"Removing achievement by id ${id.value}") {
        error => (s"Error while removing achievement: ${error.message}", error.cause)
      } { _ =>
        s"Removed achievement with id ${id.value}"
      }(storage.removeById(id))

    override def create(
                         achievement: CreateAchievement
                       ): IOWithRequestContext[Either[AppError, Achievement]] =
      surroundWithLogs[AppError, Achievement](s"Creating achievement with params $achievement") {
        error => (s"Error while creating achievement: ${error.message}", error.cause)
      } { achievement =>
        s"Created achievement $achievement"
      }(storage.create(achievement))

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
