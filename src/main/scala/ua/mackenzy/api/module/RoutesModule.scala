package ua.mackenzy.api.module

import akka.http.scaladsl.marshalling.{ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Created, NotFound, OK}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives.{as, entity, onSuccess, path, pathEnd, pathPrefix, put, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import ua.mackenzy.api.web._

import scala.concurrent.ExecutionContext

class RoutesModule extends AbstractModule with Handlers {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  def processSome[A](implicit m: ToResponseMarshaller[A]): PartialFunction[Option[A], server.Route] = {
    case Some(data) => complete(data)
    case None => complete((NotFound, HttpEntity.Empty))
  }

  def processCreatedEither[A](implicit
                              m: ToResponseMarshaller[A],
                              e: ToEntityMarshaller[A]
                             ): PartialFunction[Either[ErrorData, A], server.Route] = {
    case Right(content) => complete((Created, content))
    case Left(error) => complete((BadRequest, error))
  }

  @Inject
  @Provides
  @Singleton
  def routes(
              petController: PetController,
              userController: UserController,
              orderController: OrderController
            )
            (implicit ec: ExecutionContext): Route = {
    path("pets") {
      get {
        complete(petController.getAll)
      }
    } ~
      pathPrefix("pet" / IntNumber) { id =>
        pathEnd {
          get {
            onSuccess(petController.getBy(id))(processSome)
          }
        }
      } ~
      pathPrefix("pet") {
        pathEnd {
          post {
            entity(as[NewPetData]) { data =>
              onSuccess(petController.add(data))(processCreatedEither)
            }
          }
        }
      } ~
      path("users") {
        get {
          complete(userController.getAll)
        }
      } ~
      pathPrefix("user" / IntNumber) { id =>
        pathEnd {
          get {
            onSuccess(userController.getBy(id))(processSome)
          }
        }
      } ~
      pathPrefix("user") {
        pathEnd {
          post {
            entity(as[NewUserData]) { data =>
              complete((Created, userController.add(data)))
            }
          }
        }
      } ~
      path("store" / "orders") {
        get {
          complete(userController.getAll)
        }
      } ~
      pathPrefix("store" / "order" / IntNumber) { id =>
        pathEnd {
          get {
            onSuccess(orderController.getBy(id))(processSome)
          }
        }
      } ~
      pathPrefix("store" / "order") {
        pathEnd {
          post {
            entity(as[NewOrderData]) { data =>
              onSuccess(orderController.add(data))(processCreatedEither)
            }
          }
        }
      } ~
      pathPrefix("store" / "order" / IntNumber / "status") { id =>
        pathEnd {
          put {
            entity(as[IdData]) { data =>
              onSuccess(orderController.updateStatus(id)(data)) {
                case Right(_) => complete((OK, HttpEntity.Empty))
                case Left(error) => complete((BadRequest, error))
              }
            }
          }
        }
      }
  }
}
