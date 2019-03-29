package ua.mackenzy.api.module

import java.time.Clock

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, MethodNotAllowed, NotFound}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, RejectionHandler, _}
import io.circe.Printer
import org.slf4j.{Logger, LoggerFactory}
import ua.mackenzy.api.web.Error
import ua.mackenzy.api.web.Error.{ERROR, FAILURE}

trait Handlers {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  implicit val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit def rejectionHandler(implicit clock: Clock): RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleNotFound {
        complete((NotFound, HttpEntity.Empty))
      }
      .handle {
        case MethodRejection(_) =>
          complete((MethodNotAllowed, HttpEntity.Empty))
        case _ =>
          complete((BadRequest, Error(ERROR, "Bad request")))
      }
      .result()

  implicit def exceptionHandler(implicit clock: Clock): ExceptionHandler =
    ExceptionHandler {
      case e: IllegalArgumentException =>
        complete((BadRequest, Error(ERROR, e.getMessage)))
      case t: Throwable =>
        extractUri { uri =>
          log.error(s"Request to $uri could not be handled normally, error: '${t.getMessage}'", t)
          complete((InternalServerError, Error(FAILURE, t.getMessage)))
        }
    }

}
