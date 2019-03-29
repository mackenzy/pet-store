package ua.mackenzy.api

import java.time.{Clock, ZoneId}

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Created, NotFound, OK}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import ua.mackenzy.api.util.TimeUtils.parse

class PetApiSpec extends WordSpec with Matchers with OneServerPerSuite with ScalatestRouteTest with BeforeAndAfter {

  override def clock: Clock =
    Clock.fixed(
      parse("2019-01-01T00:00:01.000Z"),
      ZoneId.of("UTC")
    )

  before {
    resetAndInitData
  }

  "PetApi" should {
    "add pet" in {
      val json = """{"category":{"id":1,"name":"cat "},"name":"Meow ","photoUrl":"http://meoew.com "}"""
      Post("/pet").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(Created)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"id":1}""")
      }

      Get("/pet/1") ~> route ~> check {
        status should ===(OK)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"id":1,"category":{"id":1,"name":"cat"},"name":"Meow","photoUrl":"http://meoew.com"}""")
      }
    }

    "add pet with empty url" in {
      val json = """{"category":{"id":1,"name":"cat"},"name":"Meow"}"""
      Post("/pet").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(Created)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"id":1}""")
      }

      Get("/pet/1") ~> route ~> check {
        status should ===(OK)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"id":1,"category":{"id":1,"name":"cat"},"name":"Meow"}""")
      }
    }

    "return all pets" in {
      val json = """{"category":{"id":1,"name":"cat"},"name":"Meow","photoUrl":"http://meoew.com"}"""
      Post("/pet").withEntity(`application/json`, json) ~> route

      Get("/pets") ~> route ~> check {
        status should ===(OK)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""[{"id":1,"category":{"id":1,"name":"cat"},"name":"Meow","photoUrl":"http://meoew.com"}]""")
      }
    }

    "return not found" in {
      Get("/pet/3") ~> route ~> check {
        status should ===(NotFound)
        entityAs[String] should ===("")
      }
    }

    "return error on empty name when adding" in {
      val json = """{"category":{"id":1,"name":"cat"},"name":" ","photoUrl":"http://meoew.com"}"""
      Post("/pet").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"Name is empty"}""")
      }

      Get("/pets") ~> route ~> check {
        entityAs[String] should ===("[]")
      }
    }

    "return error on wrong category id when adding" in {
      val json = """{"category":{"id":3,"name":"cat"},"name":"Meow","photoUrl":"http://meoew.com"}"""
      Post("/pet").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"Pet category with id '{0}' not found","params":["3"]}""")
      }

      Get("/pets") ~> route ~> check {
        entityAs[String] should ===("[]")
      }
    }
  }
}
