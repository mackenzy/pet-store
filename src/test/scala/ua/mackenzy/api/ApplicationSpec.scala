package ua.mackenzy.api

import java.time.{Clock, ZoneId}

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import ua.mackenzy.api.util.TimeUtils.parse

class ApplicationSpec extends WordSpec with Matchers with OneServerPerSuite with ScalatestRouteTest {

  override def clock: Clock =
    Clock.fixed(
      parse("2019-01-01T00:00:01.000Z"),
      ZoneId.of("UTC")
    )

  "Application" should {
    "respond with error on get order" in {
      Get("/store/order/1") ~> route ~> check {
        status should ===(InternalServerError)
        contentType should ===(`application/json`)
        entityAs[String] should ===(
          """{"at":"2019-01-01T00:00:01Z","type":"FAILURE","message":"[SQLITE_ERROR] SQL error or missing database (no such table: orders)"}"""
        )
      }
    }
  }
}
