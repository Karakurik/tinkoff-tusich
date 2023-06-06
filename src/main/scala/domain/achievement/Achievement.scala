package domain.achievement

import derevo.circe.{decoder, encoder}
import derevo.derive
import domain.tusich.TusichId
import sttp.tapir.Schema
import tofu.logging.derivation._

@derive(loggable, encoder, decoder)
final case class CreateAchievement(name: AchievementName, tusichId: TusichId)

@derive(loggable, encoder, decoder)
final case class Achievement(id: AchievementId, name: AchievementName, tusichId: TusichId)

object Achievement {
  implicit val schema: Schema[Achievement] = Schema.derived
}
