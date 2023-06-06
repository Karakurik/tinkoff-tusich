package controller.tusich

import domain._
import domain.tusich.{Tusich, TusichId, TusichName, CreateTusich}
import domain.errors._
import domain.tusich.TusichId
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object endpoints {
  val listTusich: PublicEndpoint[RequestContext, AppError, List[Tusich], Any] =
    endpoint.get
      .in("tusich")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[Tusich]])

  val findTusichById
      : PublicEndpoint[(TusichId, RequestContext), AppError, Option[Tusich], Any] =
    endpoint.get
      .in("tusich")
      .in(path[TusichId]("tusich-id"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[Tusich]])

  val findTusichByName
  : PublicEndpoint[(TusichName, RequestContext), AppError, Option[Tusich], Any] =
    endpoint.get
      .in("tusich")
      .in(query[TusichName]("name"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[Tusich]])

  val removeTusich
      : PublicEndpoint[(TusichId, RequestContext), AppError, Unit, Any] =
    endpoint.delete
      .in("tusich" / path[TusichId])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])

  val createTusich
      : PublicEndpoint[(CreateTusich, RequestContext), AppError, Tusich, Any] =
    endpoint.post
      .in("tusich")
      .in(jsonBody[CreateTusich])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Tusich])
}
