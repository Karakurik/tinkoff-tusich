import cats.data.ReaderT
import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.comcast.ip4s._
import config.AppConfig
import controller.achievement.AchievementController
import controller.tusich.TusichController
import controller.user.UserController
import dao.achievement.AchievementSql
import dao.tusich.TusichSql
import dao.user.UserSql
import domain.{IOWithRequestContext, RequestContext}
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router
import service.achievement.AchievementStorage
import service.tusich.TusichStorage
import service.user.UserStorage
import sttp.tapir.server.http4s.Http4sServerInterpreter
import tofu.logging.Logging

object Main extends IOApp {

  private val mainLogs =
    Logging.Make.plain[IO].byName("Main")

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- Resource.eval(mainLogs.info("Starting tinkoff-tusich service..."))
      config <- Resource.eval(AppConfig.load)
      transactor = Transactor
        .fromDriverManager[IO](
          config.db.driver,
          config.db.url,
          config.db.user,
          config.db.password
        )
        .mapK[IOWithRequestContext](ReaderT.liftK[IO, RequestContext])
      achievementSql = AchievementSql.make
      tusichSql = TusichSql.make
      userSql = UserSql.make

      achievementStorage = AchievementStorage.make(achievementSql, transactor)
      tusichStorage = TusichStorage.make(tusichSql, transactor)
      userStorage = UserStorage.make(userSql, transactor)

      achievementController = AchievementController.make(achievementStorage)
      tusichController = TusichController.make(tusichStorage)
      userController = UserController.make(userStorage)

      achievementRoutes = Http4sServerInterpreter[IO]().toRoutes(achievementController.all)
      tusichRoutes = Http4sServerInterpreter[IO]().toRoutes(tusichController.all)
      userRoutes = Http4sServerInterpreter[IO]().toRoutes(userController.all)

      combinedRoutes: HttpRoutes[IO] = achievementRoutes <+> tusichRoutes <+> userRoutes
      httpApp = Router("/" -> combinedRoutes).orNotFound

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(
          Ipv4Address.fromString(config.server.host).getOrElse(ipv4"0.0.0.0")
        )
        .withPort(Port.fromInt(config.server.port).getOrElse(port"80"))
        .withHttpApp(httpApp)
        .build
    } yield ()).useForever.as(ExitCode.Success)
}
