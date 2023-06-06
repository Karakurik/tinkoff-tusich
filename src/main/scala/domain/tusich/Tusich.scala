package domain.tusich

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema
import tofu.logging.derivation._

@derive(loggable, encoder, decoder)
final case class CreateTusich(name: TusichName)

@derive(loggable, encoder, decoder)
final case class Tusich(id: TusichId, name: TusichName)

object Tusich {
  implicit val schema: Schema[Tusich] = Schema.derived
}
