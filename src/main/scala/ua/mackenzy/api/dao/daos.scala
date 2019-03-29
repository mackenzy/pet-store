package ua.mackenzy.api.dao

import com.google.inject.{Inject, Singleton}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import scala.concurrent.Future

//TODO: cache DAOs by wrapping into proxy or use actors for caching and get rid of hitting the DB on each request

@Singleton
class PetDao @Inject()(val dbConfig: DatabaseConfig[JdbcProfile]) extends DbSupport {

  import dbConfig.profile.api._

  def getAll: Future[Seq[Pet]] =
    db.run(TableQuery[PetTable].result)

  def getBy(id: Int): Future[Option[Pet]] =
    db.run(
      TableQuery[PetTable]
        .filter(r => r.id === id)
        .result
        .headOption
    )

  def add(pet: Pet): Future[Int] = {
    val pets = TableQuery[PetTable]
    val query = pets returning pets.map(_.id)
    db.run(query += pet)
  }
}

@Singleton
class PetCategoryDao @Inject()(val dbConfig: DatabaseConfig[JdbcProfile]) extends DbSupport {

  import dbConfig.profile.api._

  def getAll: Future[Seq[PetCategory]] =
    db.run(TableQuery[PetCategoryTable].result)

  def getBy(id: Int): Future[Option[PetCategory]] =
    db.run(
      TableQuery[PetCategoryTable]
        .filter(r => r.id === id)
        .result
        .headOption
    )
}

@Singleton
class UserDao @Inject()(val dbConfig: DatabaseConfig[JdbcProfile]) extends DbSupport {

  import dbConfig.profile.api._

  def getAll: Future[Seq[User]] =
    db.run(TableQuery[UserTable].result)

  def getBy(id: Int): Future[Option[User]] =
    db.run(
      TableQuery[UserTable]
        .filter(r => r.id === id)
        .result
        .headOption
    )

  def add(user: User): Future[Int] = {
    val users = TableQuery[UserTable]
    val query = users returning users.map(_.id)
    db.run(query += user)
  }
}

@Singleton
class OrderDao @Inject()(val dbConfig: DatabaseConfig[JdbcProfile]) extends DbSupport {

  import dbConfig.profile.api._

  def getAll: Future[Seq[Order]] =
    db.run(TableQuery[OrderTable].result)

  def getBy(id: Int): Future[Option[Order]] =
    db.run(
      TableQuery[OrderTable]
        .filter(r => r.id === id)
        .result
        .headOption
    )

  //TODO: check for uniqueness or implement idempotency
  def add(order: Order): Future[Int] = {
    val orders = TableQuery[OrderTable]
    val query = orders returning orders.map(_.id)
    db.run(query += order)
  }

  def update(order: Order): Future[Int] =
    db.run(
      TableQuery[OrderTable]
        .filter(r => r.id === order.id)
        .update(order)
    )
}
