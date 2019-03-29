package ua.mackenzy.api.web

import com.google.inject.{Inject, Singleton}
import ua.mackenzy.api.dao.UserDao

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(usersDao: UserDao)(implicit ec: ExecutionContext) extends Converters {

  def getAll: Future[Seq[UserData]] =
    usersDao
      .getAll
      .map(_.map(asUserData))

  def add(user: NewUserData): Future[IdData] =
    usersDao
      .add(user.validated)
      .map(IdData)

  def getBy(id: Int): Future[Option[UserData]] =
    usersDao
      .getBy(id)
      .map(_.map(asUserData))
}
