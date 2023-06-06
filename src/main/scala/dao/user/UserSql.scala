package dao.user

import cats.data.OptionT
import cats.implicits.{catsSyntaxEitherId, _}
import dao.achievement.AchievementSql
import domain.achievement.AchievementId
import domain.user._
import domain.user.errors.{UserAlreadyExists, UserNotFoundById}
import domain.userAchievement._
import domain.userAchievement.errors.UserAchievementNotFoundById
import doobie._
import doobie.implicits._


trait UserSql {
  def listAll: ConnectionIO[List[User]]

  def findById(id: UserId): ConnectionIO[Option[User]]

  def findByFirstNameAndLastName(firstName: UserFirstName, lastName: UserLastName): ConnectionIO[Option[User]]

  def removeById(id: UserId): ConnectionIO[Either[UserNotFoundById, Unit]]

  def create(user: CreateUser): ConnectionIO[Either[UserAlreadyExists, User]]

  def findUserAchievement(userId: UserId, achievementId: AchievementId): ConnectionIO[Option[ReadUserAchievement]]

  def findUserAchievementById(userAchievementId: UserAchievementId): ConnectionIO[Option[ReadUserAchievement]]

  def createUserAchievement(user: CreateUserAchievement): ConnectionIO[Option[ReadUserAchievement]]

  def updateUserAchievementLevel(userAchievementId: UserAchievementId, level: UserAchievementLevel): ConnectionIO[Either[UserAchievementNotFoundById, Unit]]
}

object UserSql {

  def make: UserSql = new Impl

  private final class Impl extends UserSql {

    import sqls._

    override def listAll: ConnectionIO[List[User]] = {
      for {
        users <- listAllSql.to[List]
        usersWithAchievements <- users.traverse { readUser =>
          for {
            readUserAchievements <- AchievementSql.sqls.listAllByUserIdSql(readUser.id).to[List]
            userAchievements <- readUserAchievements.traverseFilter { readUserAchievement =>
              AchievementSql.sqls.findByIdSql(readUserAchievement.achievementId).option.map(_.map { achievement =>
                UserAchievement(readUserAchievement.id, readUser.id, achievement, readUserAchievement.level)
              })
            }
          } yield User(readUser.id, readUser.firstName, readUser.lastName, userAchievements)
        }
      } yield usersWithAchievements
    }


    override def findById(id: UserId): ConnectionIO[Option[User]] =
      findByIdSql(id).option.flatMap {
        case Some(readUser) =>
          for {
            readUserAchievements <- AchievementSql.sqls.listAllByUserIdSql(readUser.id).to[List]
            userAchievements <- readUserAchievements.traverseFilter { readUserAchievement =>
              AchievementSql.sqls.findByIdSql(readUserAchievement.achievementId).option.map(_.map { achievement =>
                UserAchievement(readUserAchievement.id, readUser.id, achievement, readUserAchievement.level)
              })
            }
          } yield Some(User(readUser.id, readUser.firstName, readUser.lastName, userAchievements))
      }

    override def findByFirstNameAndLastName(firstName: UserFirstName, lastName: UserLastName): ConnectionIO[Option[User]] =
      findByFirstNameAndLastNameSql(firstName, lastName).option.flatMap {
        case Some(readUser) =>
          for {
            readUserAchievements <- AchievementSql.sqls.listAllByUserIdSql(readUser.id).to[List]
            userAchievements <- readUserAchievements.traverseFilter { readUserAchievement =>
              AchievementSql.sqls.findByIdSql(readUserAchievement.achievementId).option.map(_.map { achievement =>
                UserAchievement(readUserAchievement.id, readUser.id, achievement, readUserAchievement.level)
              })
            }
          } yield Some(User(readUser.id, readUser.firstName, readUser.lastName, userAchievements))
      }

    override def removeById(
                             id: UserId
                           ): ConnectionIO[Either[UserNotFoundById, Unit]] =
      removeByIdSql(id).run.map {
        case 0 => UserNotFoundById(id).asLeft[Unit]
        case _ => ().asRight[UserNotFoundById]
      }

    override def create(
                         user: CreateUser
                       ): ConnectionIO[Either[UserAlreadyExists, User]] =
      findByFirstNameAndLastNameSql(user.firstName, user.lastName).option.flatMap {
        case None =>
          insertSql(user)
            .withUniqueGeneratedKeys[UserId]("id")
            .map(id =>
              User(id, user.firstName, user.lastName, List()).asRight[UserAlreadyExists]
            )
        case Some(_) => UserAlreadyExists().asLeft[User].pure[ConnectionIO]
      }

    override def createUserAchievement(userAchievement: CreateUserAchievement): ConnectionIO[Option[ReadUserAchievement]] = {
      for {
        maybeOldUserAchievement <- findUserAchievementSql(userAchievement.userId, userAchievement.achievementId).option
        result <- maybeOldUserAchievement match {
          case Some(oldUserAchievement) => {
            val newUserAchievement = oldUserAchievement.copy(level = UserAchievementLevel(oldUserAchievement.level.value + 1))
            for {
              _ <- updateUserAchievementLevelSql(oldUserAchievement.id, newUserAchievement.level).run
            } yield Some(newUserAchievement)
          }
          case None =>
            for {
              id <- createUserAchievementSql(userAchievement).withUniqueGeneratedKeys[UserAchievementId]("id")
              newUserAchievement <- findUserAchievementByIdSql(id).option
            } yield newUserAchievement
        }
      } yield result
    }

    override def findUserAchievement(userId: UserId, achievementId: AchievementId): ConnectionIO[Option[ReadUserAchievement]] =
      findUserAchievementSql(userId, achievementId).option

    override def findUserAchievementById(userAchievementId: UserAchievementId): ConnectionIO[Option[ReadUserAchievement]] =
      findUserAchievementByIdSql(userAchievementId).option

    override def updateUserAchievementLevel(userAchievementId: UserAchievementId, level: UserAchievementLevel): ConnectionIO[Either[UserAchievementNotFoundById, Unit]] = {
      (for {
        _ <- OptionT(findUserAchievementByIdSql(userAchievementId).option)
        _ <- OptionT.liftF(updateUserAchievementLevelSql(userAchievementId, level).run)
      } yield ()).value.map {
        case Some(_) => ().asRight[UserAchievementNotFoundById]
        case None => UserAchievementNotFoundById(userAchievementId).asLeft[Unit]
      }
    }
  }

  object sqls {
    val listAllSql: Query0[ReadUser] = sql"select * from USERS".query[ReadUser]

    def findByIdSql(id: UserId): Query0[ReadUser] =
      sql"select * from USERS where id=${id.value}".query[ReadUser]

    def findByFirstNameAndLastNameSql(firstName: UserFirstName, lastName: UserLastName): Query0[ReadUser] =
      sql"select * from USERS where first_name=${firstName.value} and last_name=${lastName.value}".query[ReadUser]

    def removeByIdSql(id: UserId): Update0 =
      sql"delete from USERS where id=${id.value}".update

    def insertSql(user: CreateUser): Update0 =
      sql"insert into USERS (first_name, last_name) values (${user.firstName.value}, ${user.lastName.value})".update

    def createUserAchievementSql(userAchievement: CreateUserAchievement): Update0 =
      sql"insert into USER_ACHIEVEMENT (user_id, achievement_id, level) values (${userAchievement.userId.value}, ${userAchievement.achievementId.value}, 1)".update

    def findUserAchievementSql(userId: UserId, achievementId: AchievementId): Query0[ReadUserAchievement] =
      sql"select * from USER_ACHIEVEMENT where user_id=${userId.value} and achievement_id=${achievementId.value}".query[ReadUserAchievement]

    def findUserAchievementByIdSql(userAchievementId: UserAchievementId): Query0[ReadUserAchievement] =
      sql"select * from USER_ACHIEVEMENT where id=${userAchievementId.value}".query[ReadUserAchievement]

    def updateUserAchievementLevelSql(userAchievementId: UserAchievementId, level: UserAchievementLevel): Update0 =
      sql"update USER_ACHIEVEMENT set level=${level.value} where id=${userAchievementId.value}".update
  }
}
