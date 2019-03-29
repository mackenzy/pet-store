package ua.mackenzy.api.web

import java.time.Clock

import com.google.inject.{Inject, Singleton}
import ua.mackenzy.api.dao._
import ua.mackenzy.api.util._
import ua.mackenzy.api.web.Error.ERROR

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PetController @Inject()(
                               val petDao: PetDao,
                               val categoryDao: PetCategoryDao
                             )(implicit ec: ExecutionContext, clock: Clock) extends Converters {

  def getAll: Future[Seq[PetData]] =
    for {
      ctgs <- categoryDao.getAll.map(_.map(c => c.id -> c).toMap)
      pets <- petDao.getAll
    } yield pets.map(p => asPetData(ctgs(p.categoryId), p))

  def add(pet: NewPetData): Future[Either[ErrorData, IdData]] = {
    val catId = pet.validated.category.id
    for {
      _ <- FutEith(categoryDao.getBy(catId).toEither(Error(ERROR, "Pet category with id '{0}' not found", catId)))
      id <- FutEith(petDao.add(pet).map(Right(_)))
    } yield IdData(id)
  }.value

  def getBy(id: Int): Future[Option[PetData]] = (
    for {
      pet <- FutOpt(petDao.getBy(id))
      cat <- FutOpt(categoryDao.getBy(pet.categoryId))
    } yield asPetData(cat, pet)).value
}
