package domain.participation

import derevo.circe.{decoder, encoder}
import derevo.derive
import domain.tusich.{Tusich, TusichId, TusichName}
import domain.user.{User, UserId}
import sttp.tapir.Schema
import tofu.logging.derivation._

@derive(loggable, encoder, decoder)
final case class CreateParticipation(userId: UserId, tusichId: TusichId)

@derive(loggable, encoder, decoder)
final case class Participation(id: ParticipationId, user: User, tusich: Tusich)

object Participation {
  implicit val schema: Schema[Participation] = Schema.derived
}
