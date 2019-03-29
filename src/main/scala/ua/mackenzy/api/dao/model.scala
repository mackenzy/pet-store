package ua.mackenzy.api.dao

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

//TODO: use Long iDs
case class Pet(id: Int, categoryId: Int, name: String, photoUrl: Option[String])

case class PetCategory(id: Int, name: String)

case class User(id: Int, name: String)

case class Order(id: Int, statusId: Int, petId: Int, userId: Int) {
  def withStatus(id: Int): Order = copy(statusId = id)
}

object OrderStatus {
  val PLACED = 1
  val PROCESSING = 2
  val COMPLETED = 3

  val id2name: Map[Int, String] =
    Map(
      PLACED -> "placed",
      PROCESSING -> "processing",
      COMPLETED -> "completed"
    )

  val ids: Set[Int] =
    id2name.keys.toSet
}

trait DbSupport {
  val dbConfig: DatabaseConfig[JdbcProfile]
  val db = dbConfig.db

  import dbConfig.profile.api._

  class PetTable(tag: Tag) extends Table[Pet](tag, "pets") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def categoryId = column[Int]("category_id")

    def name = column[String]("name")

    def photoUrl = column[Option[String]]("photo_url")

    def * = (id, categoryId, name, photoUrl) <> (Pet.tupled, Pet.unapply)
  }

  class PetCategoryTable(tag: Tag) extends Table[PetCategory](tag, "pets_categories") {

    def id = column[Int]("id", O.PrimaryKey)

    def name = column[String]("value")

    def * = (id, name) <> (PetCategory.tupled, PetCategory.unapply)
  }

  class UserTable(tag: Tag) extends Table[User](tag, "users") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id, name) <> (User.tupled, User.unapply)
  }

  class OrderTable(tag: Tag) extends Table[Order](tag, "orders") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def statusId = column[Int]("status_id")

    def petId = column[Int]("pet_id")

    def userId = column[Int]("user_id")

    def * = (id, statusId, petId, userId) <> (Order.tupled, Order.unapply)
  }

}
