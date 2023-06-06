package service.tusich

import cats.syntax.applicativeError._
import cats.syntax.either._
import dao.tusich.TusichSql
import domain._
import domain.errors._
import domain.tusich._
import doobie._
import doobie.implicits._
import tofu.logging.Logging

trait TusichStorage {
  def list: IOWithRequestContext[Either[InternalError, List[Tusich]]]

  def findById(
                id: TusichId
              ): IOWithRequestContext[Either[InternalError, Option[Tusich]]]

  def findByName(
                  name: TusichName
                ): IOWithRequestContext[Either[InternalError, Option[Tusich]]]

  def removeById(id: TusichId): IOWithRequestContext[Either[AppError, Unit]]

  def create(tusich: CreateTusich): IOWithRequestContext[Either[AppError, Tusich]]
}

object TusichStorage {
  def make(
            sql: TusichSql,
            transactor: Transactor[IOWithRequestContext]
          ): TusichStorage = {
    implicit val logs =
      Logging.Make
        .contextual[IOWithRequestContext, RequestContext]
        .forService[TusichStorage]
    val storage = new Impl(sql, transactor)
    new LoggingImpl(storage)
  }

  private final class Impl(
                            sql: TusichSql,
                            transactor: Transactor[IOWithRequestContext]
                          ) extends TusichStorage {
    override def list: IOWithRequestContext[Either[InternalError, List[Tusich]]] =
      sql.listAll.transact(transactor).attempt.map(_.leftMap(InternalError(_)))

    override def findById(
                           id: TusichId
                         ): IOWithRequestContext[Either[InternalError, Option[Tusich]]] = {
      sql
        .findById(id)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
    }

    override def findByName(
                             name: TusichName
                           ): IOWithRequestContext[Either[InternalError, Option[Tusich]]] = {
      sql
        .findByName(name)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
    }

    override def removeById(
                             id: TusichId
                           ): IOWithRequestContext[Either[AppError, Unit]] =
      sql.removeById(id).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Unit]
        case Right(Left(error)) => error.asLeft[Unit]
        case _ => ().asRight[AppError]
      }

    override def create(
                         tusich: CreateTusich
                       ): IOWithRequestContext[Either[AppError, Tusich]] =
      sql.create(tusich).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Tusich]
        case Right(Left(error)) => error.asLeft[Tusich]
        case Right(Right(tusich)) => tusich.asRight[AppError]
      }
  }

  private final class LoggingImpl(storage: TusichStorage)(implicit
                                                          logging: Logging[IOWithRequestContext]
  ) extends TusichStorage {

    override def list: IOWithRequestContext[Either[InternalError, List[Tusich]]] =
      surroundWithLogs[InternalError, List[Tusich]]("Getting all tusichs") {
        error =>
          (s"Error while getting all tusichs: ${error.message}", error.cause)
      } { result =>
        s"All tusichs: ${result.mkString}"
      }(storage.list)

    override def findById(
                           id: TusichId
                         ): IOWithRequestContext[Either[InternalError, Option[Tusich]]] =
      surroundWithLogs[InternalError, Option[Tusich]](
        s"Getting tusich by id ${id.value}"
      ) { error =>
        (s"Error while getting tusich: ${error.message}\n", error.cause)
      } { result =>
        s"Found tusich: ${result.toString}"
      }(storage.findById(id))

    override def findByName(
                             name: TusichName
                           ): IOWithRequestContext[Either[InternalError, Option[Tusich]]] =
      surroundWithLogs[InternalError, Option[Tusich]](
        s"Getting tusich by name ${name.value}"
      ) { error =>
        (s"Error while getting tusich: ${error.message}\n", error.cause)
      } { result =>
        s"Found tusich: ${result.toString}"
      }(storage.findByName(name))

    override def removeById(
                             id: TusichId
                           ): IOWithRequestContext[Either[AppError, Unit]] =
      surroundWithLogs[AppError, Unit](s"Removing tusich by id ${id.value}") {
        error => (s"Error while removing tusich: ${error.message}", error.cause)
      } { _ =>
        s"Removed tusich with id ${id.value}"
      }(storage.removeById(id))

    override def create(
                         tusich: CreateTusich
                       ): IOWithRequestContext[Either[AppError, Tusich]] =
      surroundWithLogs[AppError, Tusich](s"Creating tusich with params $tusich") {
        error => (s"Error while creating tusich: ${error.message}", error.cause)
      } { tusich =>
        s"Created tusich $tusich"
      }(storage.create(tusich))

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
