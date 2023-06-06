package domain

import io.circe.{Decoder, Encoder, HCursor, Json}
import sttp.tapir.Schema
import cats.implicits.catsSyntaxOptionId

object errors {
  abstract class AppError(
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

  case class InternalError(cause0: Throwable) extends AppError("Internal error", cause0.some)
  case class MockError(override val message: String) extends AppError(message)
}
