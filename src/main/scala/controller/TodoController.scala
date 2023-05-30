package controller

import cats.effect.IO
import cats.syntax.either._
import controller.endpoints._
import domain.errors._
import service.TodoStorage
import sttp.tapir.server.ServerEndpoint

trait TodoController {
  def listAllTodos: ServerEndpoint[Any, IO]
  def findTodoById: ServerEndpoint[Any, IO]
  def removeTodoById: ServerEndpoint[Any, IO]
  def createTodo: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object TodoController {
  final private class Impl(storage: TodoStorage) extends TodoController {

    override val listAllTodos: ServerEndpoint[Any, IO] =
      listTodos.serverLogic { ctx =>
        storage.list.map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val findTodoById: ServerEndpoint[Any, IO] =
      endpoints.findTodoById.serverLogic { case (id, ctx) =>
        storage.findById(id).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val removeTodoById: ServerEndpoint[Any, IO] =
      endpoints.removeTodo.serverLogic { case (id, ctx) =>
        storage.removeById(id).run(ctx)
      }

    override val createTodo: ServerEndpoint[Any, IO] =
      endpoints.createTodo.serverLogic { case (ctx, todo) =>
        storage.create(todo).run(ctx)
      }

    override val all: List[ServerEndpoint[Any, IO]] = List(
      listAllTodos,
      findTodoById,
      removeTodoById,
      createTodo
    )
  }

  def make(storage: TodoStorage): TodoController = new Impl(storage)
}
