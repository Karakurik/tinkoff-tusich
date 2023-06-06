package dao.tusich

import cats.syntax.applicative._
import cats.syntax.either._
import domain.tusich.errors.{TusichAlreadyExists, TusichNotFoundById}
import domain.tusich.{CreateTusich, Tusich, TusichId, TusichName}
import doobie._
import doobie.implicits._

trait TusichSql {
  def listAll: ConnectionIO[List[Tusich]]

  def findById(id: TusichId): ConnectionIO[Option[Tusich]]

  def findByName(name: TusichName): ConnectionIO[Option[Tusich]]

  def removeById(id: TusichId): ConnectionIO[Either[TusichNotFoundById, Unit]]

  def create(tusich: CreateTusich): ConnectionIO[Either[TusichAlreadyExists, Tusich]]
}

object TusichSql {
  def make: TusichSql = new Impl

  private final class Impl extends TusichSql {

    import sqls._

    override def listAll: ConnectionIO[List[Tusich]] =
      listAllSql.to[List]

    override def findById(
                           id: TusichId
                         ): ConnectionIO[Option[Tusich]] =
      findByIdSql(id).option

    override def findByName(
                             name: TusichName
                           ): ConnectionIO[Option[Tusich]] =
      findByNameSql(name).option

    override def removeById(
                             id: TusichId
                           ): ConnectionIO[Either[TusichNotFoundById, Unit]] =
      removeByIdSql(id).run.map {
        case 0 => TusichNotFoundById(id).asLeft[Unit]
        case _ => ().asRight[TusichNotFoundById]
      }

    override def create(
                         tusich: CreateTusich
                       ): ConnectionIO[Either[TusichAlreadyExists, Tusich]] =
      findByNameSql(tusich.name).option.flatMap {
        case None =>
          insertSql(tusich)
            .withUniqueGeneratedKeys[TusichId]("id")
            .map(id =>
              Tusich(id, tusich.name).asRight[TusichAlreadyExists]
            )
        case Some(_) => TusichAlreadyExists().asLeft[Tusich].pure[ConnectionIO]
      }
  }

  object sqls {
    val listAllSql: Query0[Tusich] = sql"select * from tusich".query[Tusich]

    def findByIdSql(id: TusichId): Query0[Tusich] =
      sql"select * from tusich where id=${id.value}".query[Tusich]

    def removeByIdSql(id: TusichId): Update0 =
      sql"delete from tusich where id=${id.value}".update

    def insertSql(tusich: CreateTusich): Update0 =
      sql"insert into tusich (name) values (${tusich.name.value})".update

    def findByNameSql(name: TusichName) =
      sql"select * from tusich where name=${name.value}"
        .query[Tusich]
  }
}
