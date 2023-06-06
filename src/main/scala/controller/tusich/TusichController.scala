package controller.tusich

import cats.effect.IO
import cats.syntax.either._
import domain.errors._
import service.tusich.TusichStorage
import sttp.tapir.server.ServerEndpoint

trait TusichController {
  def listAllTusich: ServerEndpoint[Any, IO]
  def findTusichById: ServerEndpoint[Any, IO]
  def findTusichByName: ServerEndpoint[Any, IO]
  def removeTusichById: ServerEndpoint[Any, IO]
  def createTusich: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object TusichController {
  final private class Impl(storage: TusichStorage) extends TusichController {

    override val listAllTusich: ServerEndpoint[Any, IO] =
      endpoints.listTusich.serverLogic { ctx =>
        storage.list.map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val findTusichById: ServerEndpoint[Any, IO] =
      endpoints.findTusichById.serverLogic { case (id, ctx) =>
        storage.findById(id).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val findTusichByName: ServerEndpoint[Any, IO] =
      endpoints.findTusichByName.serverLogic { case (name, ctx) =>
        storage.findByName(name).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val removeTusichById: ServerEndpoint[Any, IO] =
      endpoints.removeTusich.serverLogic { case (id, ctx) =>
        storage.removeById(id).run(ctx)
      }

    override val createTusich: ServerEndpoint[Any, IO] =
      endpoints.createTusich.serverLogic { case ((todo, ctx)) =>
        storage.create(todo).run(ctx)
      }

    override val all: List[ServerEndpoint[Any, IO]] = List(
      listAllTusich,
      findTusichById,
      findTusichByName,
      removeTusichById,
      createTusich
    )
  }

  def make(storage: TusichStorage): TusichController = new Impl(storage)
}
