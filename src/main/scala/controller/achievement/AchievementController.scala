package controller.achievement

import cats.effect.IO
import cats.syntax.either._
import domain.errors._
import service.achievement.AchievementStorage
import sttp.tapir.server.ServerEndpoint

trait AchievementController {
  def listAllAchievement: ServerEndpoint[Any, IO]

  def listAllAchievementByTusichId: ServerEndpoint[Any, IO]

  def findAchievementById: ServerEndpoint[Any, IO]

  def findAchievementByName: ServerEndpoint[Any, IO]

  def removeAchievementById: ServerEndpoint[Any, IO]

  def createAchievement: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object AchievementController {
  def make(storage: AchievementStorage): AchievementController = new Impl(storage)

  final private class Impl(storage: AchievementStorage) extends AchievementController {

    override val listAllAchievement: ServerEndpoint[Any, IO] =
      endpoints.listAchievement.serverLogic { ctx =>
        storage.list.map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val listAllAchievementByTusichId: ServerEndpoint[Any, IO] =
      endpoints.listAchievementByTusichId.serverLogic { case (tusichId, ctx) =>
        storage.listByTusichId(tusichId).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val findAchievementById: ServerEndpoint[Any, IO] =
      endpoints.findAchievementById.serverLogic { case (id, ctx) =>
        storage.findById(id).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val findAchievementByName: ServerEndpoint[Any, IO] =
      endpoints.findAchievementByName.serverLogic { case (name, ctx) =>
        storage.findByName(name).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val removeAchievementById: ServerEndpoint[Any, IO] =
      endpoints.removeAchievement.serverLogic { case (id, ctx) =>
        storage.removeById(id).run(ctx)
      }

    override val createAchievement: ServerEndpoint[Any, IO] =
      endpoints.createAchievement.serverLogic { case (todo, ctx) =>
        storage.create(todo).run(ctx)
      }

    override val all: List[ServerEndpoint[Any, IO]] = List(
      listAllAchievement,
      listAllAchievementByTusichId,
      findAchievementById,
      findAchievementByName,
      removeAchievementById,
      createAchievement
    )
  }
}
