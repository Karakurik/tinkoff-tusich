package controller.achievement

import domain._
import domain.achievement.{Achievement, AchievementId, AchievementName, CreateAchievement}
import domain.errors._
import domain.tusich.TusichId
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object endpoints {
  val listAchievement: PublicEndpoint[RequestContext, AppError, List[Achievement], Any] =
    endpoint.get
      .in("achievement")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[Achievement]])

  val listAchievementByTusichId: PublicEndpoint[(TusichId, RequestContext), AppError, List[Achievement], Any] =
    endpoint.get
      .in("achievement")
      .in(query[TusichId]("tusich_id"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[Achievement]])

  val findAchievementById
      : PublicEndpoint[(AchievementId, RequestContext), AppError, Option[Achievement], Any] =
    endpoint.get
      .in("achievement")
      .in(path[AchievementId]("achievement-id"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[Achievement]])

  val findAchievementByName
  : PublicEndpoint[(AchievementName, RequestContext), AppError, Option[Achievement], Any] =
    endpoint.get
      .in("achievement")
      .in(query[AchievementName]("name"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[Achievement]])

  val removeAchievement
      : PublicEndpoint[(AchievementId, RequestContext), AppError, Unit, Any] =
    endpoint.delete
      .in("achievement" / path[AchievementId])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])

  val createAchievement
      : PublicEndpoint[(CreateAchievement, RequestContext), AppError, Achievement, Any] =
    endpoint.post
      .in("achievement")
      .in(jsonBody[CreateAchievement])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Achievement])
}
