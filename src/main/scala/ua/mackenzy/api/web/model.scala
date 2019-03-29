package ua.mackenzy.api.web

import java.time.{Clock, Instant}

import ua.mackenzy.api.dao._

case class PetCategoryData(id: Int, name: String)

case class PetData(id: Int, category: PetCategoryData, name: String, photoUrl: Option[String])

case class PetViewData(category: PetCategoryData, name: String, photoUrl: Option[String])

case class NewPetData(category: PetCategoryData, name: String, photoUrl: Option[String]) {

  def validated: NewPetData = {
    if (name.trim.isEmpty) throw new IllegalArgumentException(s"Name is empty")
    if (photoUrl.exists(_.trim.isEmpty)) throw new IllegalArgumentException(s"PhotoUrl is empty")
    this
  }
}

case class IdData(id: Int)

case class UserData(id: Int, name: String)

case class NewUserData(name: String) {

  def validated: NewUserData = {
    if (name.trim.isEmpty) throw new IllegalArgumentException(s"Name is empty")
    this
  }
}

case class StatusData(id: Int, value: String)

case class OrderData(pet: PetViewData, user: UserData, status: StatusData)

case class NewOrderData(petId: Int, userId: Int)

case class ErrorData(at: Instant, `type`: String, message: String, params: Option[Seq[String]] = None)

object Error {
  def apply(`type`: String, message: String, params: String*)(implicit clock: Clock): ErrorData =
    ErrorData(Instant.now(clock), `type`, message, if (params.nonEmpty) Some(params) else None)

  val ERROR = "ERROR"
  val FAILURE = "FAILURE"
}

trait Converters {

  def asPetData(petCategory: PetCategory, pet: Pet): PetData =
    PetData(pet.id, petCategory, pet.name, pet.photoUrl)

  def asPetViewData(petCategory: PetCategory, pet: Pet): PetViewData =
    PetViewData(petCategory, pet.name, pet.photoUrl)

  def asUserData(user: User): UserData =
    UserData(user.id, user.name)

  def asOrderData(order: Order, user: User, pet: Pet, category: PetCategory): OrderData = {
    val u = asUserData(user)
    val sn = asStatus(order.statusId)
    val s = StatusData(order.statusId, sn)
    val p = asPetViewData(category, pet)
    OrderData(p, u, s)
  }

  def asStatus(id: Int): String =
    OrderStatus.id2name(id)

  implicit def asPet(pet: NewPetData): Pet =
    Pet(-1, pet.category.id, pet.name.trim, pet.photoUrl.map(_.trim))

  implicit def asUser(user: NewUserData): User =
    User(-1, user.name.trim)

  implicit def asOrder(order: NewOrderData): Order =
    Order(-1, OrderStatus.PLACED, order.petId, order.userId)

  implicit def asPetCategoryData(petCategory: PetCategory): PetCategoryData =
    PetCategoryData(petCategory.id, petCategory.name)
}
