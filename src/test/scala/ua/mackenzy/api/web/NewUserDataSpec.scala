package ua.mackenzy.api.web

import org.scalatest.{Matchers, WordSpec}

class NewUserDataSpec extends WordSpec with Matchers {

  "NewUserData" should {

    "be validated" in {
      NewUserData("John Sena").validated
    }

    "throw an exception on empty name when validating" in {
      val ex = the[IllegalArgumentException] thrownBy {
        NewUserData("").validated
      }
      ex.getMessage should equal("Name is empty")
    }

    "throw an exception on blank name when validating" in {
      val ex = the[IllegalArgumentException] thrownBy {
        NewUserData(" ").validated
      }
      ex.getMessage should equal("Name is empty")
    }
  }
}
