package service

import cats.syntax.applicativeError._
import cats.syntax.either._
import dao.TodoSql
import domain._
import domain.errors._
import doobie._
import doobie.implicits._
import tofu.logging.Logging

trait TodoStorage {
  def list: IOWithRequestContext[Either[InternalError, List[Todo]]]
  def findById(
      id: TodoId
  ): IOWithRequestContext[Either[InternalError, Option[Todo]]]
  def removeById(id: TodoId): IOWithRequestContext[Either[AppError, Unit]]
  def create(todo: CreateTodo): IOWithRequestContext[Either[AppError, Todo]]
}

object TodoStorage {
  private final class Impl(
      sql: TodoSql,
      transactor: Transactor[IOWithRequestContext]
  ) extends TodoStorage {
    override def list: IOWithRequestContext[Either[InternalError, List[Todo]]] =
      sql.listAll.transact(transactor).attempt.map(_.leftMap(InternalError(_)))

    override def findById(
        id: TodoId
    ): IOWithRequestContext[Either[InternalError, Option[Todo]]] = {
      sql
        .findById(id)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
    }

    override def removeById(
        id: TodoId
    ): IOWithRequestContext[Either[AppError, Unit]] =
      sql.removeById(id).transact(transactor).attempt.map {
        case Left(th)           => InternalError(th).asLeft[Unit]
        case Right(Left(error)) => error.asLeft[Unit]
        case _                  => ().asRight[AppError]
      }

    override def create(
        todo: CreateTodo
    ): IOWithRequestContext[Either[AppError, Todo]] =
      sql.create(todo).transact(transactor).attempt.map {
        case Left(th)           => InternalError(th).asLeft[Todo]
        case Right(Left(error)) => error.asLeft[Todo]
        case Right(Right(todo)) => todo.asRight[AppError]
      }
  }

  private final class LoggingImpl(storage: TodoStorage)(implicit
      logging: Logging[IOWithRequestContext]
  ) extends TodoStorage {

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

    override def list: IOWithRequestContext[Either[InternalError, List[Todo]]] =
      surroundWithLogs[InternalError, List[Todo]]("Getting all todos") {
        error =>
          (s"Error while getting all todos: ${error.message}", error.cause)
      } { result =>
        s"All todos: ${result.mkString}"
      }(storage.list)

    override def findById(
        id: TodoId
    ): IOWithRequestContext[Either[InternalError, Option[Todo]]] =
      surroundWithLogs[InternalError, Option[Todo]](
        s"Getting todo by id ${id.value}"
      ) { error =>
        (s"Error while getting todo: ${error.message}\n", error.cause)
      } { result =>
        s"Found todo: ${result.toString}"
      }(storage.findById(id))

    override def removeById(
        id: TodoId
    ): IOWithRequestContext[Either[AppError, Unit]] =
      surroundWithLogs[AppError, Unit](s"Removing todo by id ${id.value}") {
        error => (s"Error while removing todo: ${error.message}", error.cause)
      } { _ =>
        s"Removed todo with id ${id.value}"
      }(storage.removeById(id))

    override def create(
        todo: CreateTodo
    ): IOWithRequestContext[Either[AppError, Todo]] =
      surroundWithLogs[AppError, Todo](s"Creating todo with params $todo") {
        error => (s"Error while creating todo: ${error.message}", error.cause)
      } { todo =>
        s"Created todo $todo"
      }(storage.create(todo))

  }

  def make(
      sql: TodoSql,
      transactor: Transactor[IOWithRequestContext]
  ): TodoStorage = {
    implicit val logs =
      Logging.Make
        .contextual[IOWithRequestContext, RequestContext]
        .forService[TodoStorage]
    val storage = new Impl(sql, transactor)
    new LoggingImpl(storage)
  }
}
