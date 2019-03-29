package ua.mackenzy.api.web

import org.scalatest.{Matchers, WordSpec}

class NewPetDataSpec extends WordSpec with Matchers {

  "NewPetData" should {

    "be validated" in {
      val cat = PetCategoryData(1, "category")
      NewPetData(cat, "Meow", Some("http://meow.com")).validated
    }

    "be validated with empty url" in {
      val cat = PetCategoryData(1, "category")
      NewPetData(cat, "Meow", None).validated
    }

    "throw an exception on empty name when validating" in {
      val cat = PetCategoryData(1, "category")
      val ex = the[IllegalArgumentException] thrownBy {
        NewPetData(cat, "", Some("http://meow.com")).validated
      }

      ex.getMessage should equal("Name is empty")
    }

    "throw an exception on blank name when validating" in {
      val cat = PetCategoryData(1, "category")
      val ex = the[IllegalArgumentException] thrownBy {
        NewPetData(cat, " ", Some("http://meow.com")).validated
      }

      ex.getMessage should equal("Name is empty")
    }

    "throw an exception on empty photoUrl when validating" in {
      val cat = PetCategoryData(1, "category")
      val ex = the[IllegalArgumentException] thrownBy {
        NewPetData(cat, "Meow", Some("")).validated
      }

      ex.getMessage should equal("PhotoUrl is empty")
    }

    "throw an exception on blank photoUrl when validating" in {
      val cat = PetCategoryData(1, "category")
      val ex = the[IllegalArgumentException] thrownBy {
        NewPetData(cat, "Meow", Some(" ")).validated
      }

      ex.getMessage should equal("PhotoUrl is empty")
    }

    "throw an exception on empty photoUrl and name when validating" in {
      val cat = PetCategoryData(1, "category")
      val ex = the[IllegalArgumentException] thrownBy {
        NewPetData(cat, "", Some("")).validated
      }

      ex.getMessage should equal("Name is empty")
    }
  }

}
