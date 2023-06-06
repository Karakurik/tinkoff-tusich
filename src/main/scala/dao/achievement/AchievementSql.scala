package dao.achievement

import cats.syntax.applicative._
import cats.syntax.either._
import domain.achievement.errors.{AchievementAlreadyExists, AchievementNotFoundById}
import domain.achievement.{Achievement, AchievementId, AchievementName, CreateAchievement}
import domain.tusich.TusichId
import domain.user.UserId
import domain.userAchievement.{ReadUserAchievement, UserAchievement}
import doobie._
import doobie.implicits._
import tofu.syntax.collections.TofuSequenceOps

trait AchievementSql {
  def listAll: ConnectionIO[List[Achievement]]

  def listAllByTusichId(tusuchId: TusichId): ConnectionIO[List[Achievement]]

  def listAllByUserId(userId: UserId): ConnectionIO[List[UserAchievement]]

  def findById(id: AchievementId): ConnectionIO[Option[Achievement]]

  def findByName(name: AchievementName): ConnectionIO[Option[Achievement]]

  def removeById(id: AchievementId): ConnectionIO[Either[AchievementNotFoundById, Unit]]

  def create(achievement: CreateAchievement): ConnectionIO[Either[AchievementAlreadyExists, Achievement]]
}

object AchievementSql {
  def make: AchievementSql = new Impl

  private final class Impl extends AchievementSql {

    import sqls._

    override def listAll: ConnectionIO[List[Achievement]] =
      listAllSql.to[List]

    override def listAllByTusichId(tusichId: TusichId): ConnectionIO[List[Achievement]] =
      listAllByTusichIdSql(tusichId).to[List]

    override def listAllByUserId(userId: UserId): ConnectionIO[List[UserAchievement]] =
      listAllByUserIdSql(userId).to[List].flatMap { readUserAchievements =>
        readUserAchievements.map { readUserAchievement =>
          (for {
            achievement <- findByIdSql(readUserAchievement.achievementId).option
          } yield (achievement)).map { case Some(achievement) =>
            UserAchievement(
              readUserAchievement.id,
              readUserAchievement.userId,
              achievement,
              readUserAchievement.level
            )
          }
        }.sequence
      }

    override def findById(
                           id: AchievementId
                         ): ConnectionIO[Option[Achievement]] =
      findByIdSql(id).option

    override def findByName(
                             name: AchievementName
                           ): ConnectionIO[Option[Achievement]] =
      findByNameSql(name).option

    override def removeById(
                             id: AchievementId
                           ): ConnectionIO[Either[AchievementNotFoundById, Unit]] =
      removeByIdSql(id).run.map {
        case 0 => AchievementNotFoundById(id).asLeft[Unit]
        case _ => ().asRight[AchievementNotFoundById]
      }

    override def create(
                         achievement: CreateAchievement
                       ): ConnectionIO[Either[AchievementAlreadyExists, Achievement]] = {
      findByNameAndTusichIdSql(achievement.name, achievement.tusichId).option.flatMap {
        case None =>
          insertSql(achievement)
            .withUniqueGeneratedKeys[AchievementId]("id")
            .map(id =>
              Achievement(id, achievement.name, achievement.tusichId).asRight[AchievementAlreadyExists]
            )
        case Some(_) => AchievementAlreadyExists().asLeft[Achievement].pure[ConnectionIO]
      }
    }

    private def findByNameAndTusichId(
                                       name: AchievementName,
                                       tusichId: TusichId
                                     ): ConnectionIO[Option[Achievement]] =
      findByNameAndTusichIdSql(name, tusichId).option
  }

  object sqls {
    def listAllSql: Query0[Achievement] = sql"select * from achievement".query[Achievement]

    def listAllByTusichIdSql(tusichId: TusichId): Query0[Achievement] =
      sql"select * from achievement where tusich_id=${tusichId.value}".query[Achievement]

    def listAllByUserIdSql(userId: UserId): Query0[ReadUserAchievement] =
      sql"select * from user_achievement where user_id=${userId.value}".query[ReadUserAchievement]

    def findByIdSql(id: AchievementId): Query0[Achievement] =
      sql"select * from achievement where id=${id.value}".query[Achievement]

    def findByNameSql(name: AchievementName): Query0[Achievement] =
      sql"select * from achievement where name=${name.value}".query[Achievement]

    def findByNameAndTusichIdSql(name: AchievementName, tusichId: TusichId): Query0[Achievement] =
      sql"select * from achievement where name=${name.value} and tusich_id=${tusichId.value}".query[Achievement]

    def removeByIdSql(id: AchievementId): Update0 =
      sql"delete from achievement where id=${id.value}".update

    def insertSql(achievement: CreateAchievement): Update0 =
      sql"insert into achievement (name, tusich_id) values (${achievement.name.value}, ${achievement.tusichId.value})".update
  }
}
