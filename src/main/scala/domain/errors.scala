package domain

import cats.syntax.option._
import io.circe.{Decoder, Encoder, HCursor, Json}
import sttp.tapir.Schema

object errors {
  sealed abstract class AppError(
      val message: String,
      val cause: Option[Throwable] = None
  )
  object AppError {
    implicit val encoder: Encoder[AppError] = new Encoder[AppError] {
      override def apply(a: AppError): Json = Json.obj(
        ("message", Json.fromString(a.message))
      )
    }

    implicit val decoder: Decoder[AppError] = new Decoder[AppError] {
      override def apply(c: HCursor): Decoder.Result[AppError] =
        c.downField("message").as[String].map(MockError(_))
    }

    implicit val schema: Schema[AppError] = Schema.string[AppError]
  }

  case class TodoAlreadyExists()
      extends AppError("Todo with same name and date already exists")
  case class TodoNotFound(id: TodoId)
      extends AppError(s"Todo with id ${id.value} not found")
  case class InternalError(cause0: Throwable)
      extends AppError("Internal error", cause0.some)
  case class MockError(override val message: String) extends AppError(message)
}
