package ua.mackenzy.api.web

import java.time.Clock

import akka.Done
import com.google.inject.{Inject, Singleton}
import ua.mackenzy.api.dao.OrderStatus.ids
import ua.mackenzy.api.dao._
import ua.mackenzy.api.util._
import ua.mackenzy.api.web.Error.ERROR

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(
                                 val petDao: PetDao,
                                 val categoryDao: PetCategoryDao,
                                 val userDao: UserDao,
                                 val orderDao: OrderDao
                               )(implicit ec: ExecutionContext, clock: Clock) extends Converters {

  def getBy(id: Int): Future[Option[OrderData]] = (
    for {
      order <- FutOpt(orderDao.getBy(id))
      user <- FutOpt(userDao.getBy(order.userId))
      pet <- FutOpt(petDao.getBy(order.petId))
      cat <- FutOpt(categoryDao.getBy(pet.categoryId))
    } yield asOrderData(order, user, pet, cat)).value

  def add(order: NewOrderData): Future[Either[ErrorData, IdData]] = (
    for {
      _ <- FutEith(petDao.getBy(order.petId).toEither(Error(ERROR, "Pet with id '{0}' not found", order.petId)))
      _ <- FutEith(userDao.getBy(order.userId).toEither(Error(ERROR, "User with id '{0}' not found", order.userId)))
      id <- FutEith(orderDao.add(order).map(Right(_)))
    } yield IdData(id)).value

  def updateStatus(orderId: Int)(status: IdData): Future[Either[ErrorData, Done]] =
    if (!ids.contains(status.id))
      Future.successful(Left(Error(ERROR, "Status id '{0}' does not exist", status.id)))
    else (
      for {
        o <- FutEith(orderDao.getBy(orderId).toEither(Error(ERROR, "Order with id '{0}' not found", orderId)))
        r <- FutEith(orderDao.update(o.withStatus(status.id)).map(_ => Right(Done)))
      } yield r).value
}
