package ua.mackenzy.api

import java.time.{Clock, ZoneId}

import akka.http.scaladsl.model.ContentTypes.{NoContentType, `application/json`}
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import ua.mackenzy.api.dao.OrderDao
import ua.mackenzy.api.util.TimeUtils.parse
import ua.mackenzy.api.util._

class OrderApi extends WordSpec with Matchers with OneServerPerSuite with ScalatestRouteTest with BeforeAndAfter {

  val orderDao = injector.getInstance(classOf[OrderDao])

  override def clock: Clock =
    Clock.fixed(
      parse("2019-01-01T00:00:01.000Z"),
      ZoneId.of("UTC")
    )

  before {
    resetAndInitData
    Post("/user").withEntity(`application/json`,"""{"name": "John Cena"}""") ~> route
    Post("/pet").withEntity(`application/json`,"""{"category":{"id":1,"name":"cat"},"name":"Matroskin","photoUrl":"http://matroskin.com/00001.jpg"}""") ~> route
  }

  "OrderApi" should {
    "add order" in {
      val json = """{"petId":1,"userId":1}"""
      Post("/store/order").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(Created)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"id":1}""")
      }

      Get("/store/order/1") ~> route ~> check {
        status should ===(OK)
        contentType should ===(`application/json`)
        entityAs[String] should ===(
          """{"pet":{"category":{"id":1,"name":"cat"},"name":"Matroskin","photoUrl":"http://matroskin.com/00001.jpg"},"user":{"id":1,"name":"John Cena"},"status":{"id":1,"value":"placed"}}"""
        )
      }
    }

    "update status" in {
      Post("/store/order").withEntity(`application/json`, """{"petId":1,"userId":1}""") ~> route

      Put("/store/order/1/status").withEntity(`application/json`, """{"id": 2}""") ~> route ~> check {
        status should ===(OK)
        contentType should ===(NoContentType)
        entityAs[String] should ===("")
      }

      Get("/store/order/1") ~> route ~> check {
        status should ===(OK)
        contentType should ===(`application/json`)
        entityAs[String] should ===(
          """{"pet":{"category":{"id":1,"name":"cat"},"name":"Matroskin","photoUrl":"http://matroskin.com/00001.jpg"},"user":{"id":1,"name":"John Cena"},"status":{"id":2,"value":"processing"}}"""
        )
      }
    }

    "return not found on getting unknown orderId" in {
      Get("/store/order/3") ~> route ~> check {
        status should ===(NotFound)
        entityAs[String] should ===("")
      }
    }

    "return error on unknown order id on update status" in {
      Put("/store/order/2/status").withEntity(`application/json`, """{"id": 2}""") ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"Order with id '{0}' not found","params":["2"]}""")
      }

      orderDao.getAll.await.isEmpty should equal(true)
    }

    "return error on unknown status id on update status" in {
      Put("/store/order/2/status").withEntity(`application/json`, """{"id": 4}""") ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"Status id '{0}' does not exist","params":["4"]}""")
      }

      orderDao.getAll.await.isEmpty should equal(true)
    }

    "return error on wrong body" in {
      Put("/store/order/2/status").withEntity(`application/json`, """{"ids": 4}""") ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"Bad request"}""")
      }

      orderDao.getAll.await.isEmpty should equal(true)
    }

    "return error in unknown petId on adding order" in {
      val json = """{"petId":2,"userId":1}"""
      Post("/store/order").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"Pet with id '{0}' not found","params":["2"]}""")
      }

      orderDao.getAll.await.isEmpty should equal(true)
    }

    "return error in unknown userId on adding order" in {
      val json = """{"petId":1,"userId":2}"""
      Post("/store/order").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"User with id '{0}' not found","params":["2"]}""")
      }

      orderDao.getAll.await.isEmpty should equal(true)
    }

    "return error in unknown userId and petId on adding order" in {
      val json = """{"petId":2,"userId":2}"""
      Post("/store/order").withEntity(`application/json`, json) ~> route ~> check {
        status should ===(BadRequest)
        contentType should ===(`application/json`)
        entityAs[String] should ===("""{"at":"2019-01-01T00:00:01Z","type":"ERROR","message":"Pet with id '{0}' not found","params":["2"]}""")
      }

      orderDao.getAll.await.isEmpty should equal(true)
    }
  }
}
