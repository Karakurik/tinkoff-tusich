package controller.user

import cats.effect.IO
import cats.syntax.either._
import domain.errors._
import service.user.UserStorage
import sttp.tapir.server.ServerEndpoint

trait UserController {
  def listAllUser: ServerEndpoint[Any, IO]
  def findUserById: ServerEndpoint[Any, IO]
  def findUserByFirstNameAndLAstName: ServerEndpoint[Any, IO]
  def removeUserById: ServerEndpoint[Any, IO]
  def createUser: ServerEndpoint[Any, IO]
  def createUserAchievement: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object UserController {
  final private class Impl(storage: UserStorage) extends UserController {

    override val listAllUser: ServerEndpoint[Any, IO] =
      endpoints.listUser.serverLogic { ctx =>
        storage.list.map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val findUserById: ServerEndpoint[Any, IO] =
      endpoints.findUserById.serverLogic { case (id, ctx) =>
        storage.findById(id).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val findUserByFirstNameAndLAstName: ServerEndpoint[Any, IO] =
      endpoints.findUserByFirstNameAndLastName.serverLogic { case (firstName, lastName, ctx) =>
        storage.findByFirstNameAndLastName(firstName, lastName).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val removeUserById: ServerEndpoint[Any, IO] =
      endpoints.removeUser.serverLogic { case (id, ctx) =>
        storage.removeById(id).run(ctx)
      }

    override val createUser: ServerEndpoint[Any, IO] =
      endpoints.createUser.serverLogic { case ((todo, ctx)) =>
        storage.create(todo).run(ctx)
      }

    override val createUserAchievement: ServerEndpoint[Any, IO] =
      endpoints.createUserAchievement.serverLogic { case ((userId, createUserAchievement, ctx)) =>
        storage.createUserAchievement(userId, createUserAchievement).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val all: List[ServerEndpoint[Any, IO]] = List(
      listAllUser,
      findUserById,
      findUserByFirstNameAndLAstName,
      removeUserById,
      createUser,
      createUserAchievement
    )
  }

  def make(storage: UserStorage): UserController = new Impl(storage)
}
