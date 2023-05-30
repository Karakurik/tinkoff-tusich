import cats.data.ReaderT
import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s._
import config.AppConfig
import controller.TodoController
import dao.TodoSql
import domain.{IOWithRequestContext, RequestContext}
import doobie.util.transactor.Transactor
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router
import service.TodoStorage
import sttp.tapir.server.http4s.Http4sServerInterpreter
import tofu.logging.Logging

object Main extends IOApp {

  private val mainLogs =
    Logging.Make.plain[IO].byName("Main")

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- Resource.eval(mainLogs.info("Starting Todos service..."))
      config <- Resource.eval(AppConfig.load)
      transactor = Transactor
        .fromDriverManager[IO](
          config.db.driver,
          config.db.url,
          config.db.user,
          config.db.password
        )
        .mapK[IOWithRequestContext](ReaderT.liftK[IO, RequestContext])
      sql = TodoSql.make
      storage = TodoStorage.make(sql, transactor)
      controller = TodoController.make(storage)
      routes = Http4sServerInterpreter[IO]().toRoutes(controller.all)
      httpApp = Router("/" -> routes).orNotFound

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
