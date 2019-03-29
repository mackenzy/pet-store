package ua.mackenzy.api

import java.time.{Clock, ZoneId}

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Created, OK}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import ua.mackenzy.api.util.TimeUtils.parse

class UserApiSpec extends WordSpec with Matchers with OneServerPerSuite with ScalatestRouteTest with BeforeAndAfter {

  override def clock: Clock =
    Clock.fixed(
      parse("2019-01-01T00:00:01.000Z"),
      ZoneId.of("UTC")
    )

  before {
    resetAndInitData
  }

  "UserApi" should {
    "add user" in {
      val json = """{"name": "John Cena "}"""
      Post("/user").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(Created)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"id":1}""")
      }

      Get("/user/1") ~> route ~> check {
        status should ===(OK)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"id":1,"name":"John Cena"}""")
      }
    }

    "return all users" in {
      Post("/user").withEntity(`application/json`,"""{"name": "John Cena"}""") ~> route
      Post("/user").withEntity(`application/json`,"""{"name": "Eric Cartman"}""") ~> route

      Get("/users") ~> route ~> check {
        status should ===(OK)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""[{"id":1,"name":"John Cena"},{"id":2,"name":"Eric Cartman"}]""")
      }
    }

    "return user not found" in {
      val getRequest = Get("/user/2")

      getRequest ~> route ~> check {
        status should ===(StatusCodes.NotFound)
        entityAs[String] should ===("")
      }
    }

    "return no users" in {
      Get("/users") ~> route ~> check {
        status should ===(OK)
        contentType should ===(`application/json`)
        entityAs[String] should ===("[]")
      }
    }

    "return error on adding user" in {
      val json = """{"name": " "}"""
      Post("/user").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"Name is empty"}""")
      }

      Get("/users") ~> route ~> check {
        entityAs[String] should ===("[]")
      }
    }
  }
}
