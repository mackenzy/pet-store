package ua.mackenzy.api

import java.time.Instant
import java.time.format.DateTimeFormatter

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

package object util {

  implicit class FutureImplicitUtils[T](f: Future[T]) {
    def await: T = Await.result(f, Duration.Inf)
  }

  implicit class FutureOptImplicitUtils[A](f: Future[Option[A]]) {
    def toEither[B](left: => B)(implicit ec: ExecutionContext): Future[Either[B, A]] =
      f.map {
        case Some(x) => Right(x)
        case None => Left(left)
      }
  }

  implicit def int2String(i: Int): String = i.toString

  case class FutOpt[A](value: Future[Option[A]]) {

    def map[B](f: A => B)(implicit ec: ExecutionContext): FutOpt[B] =
      copy(value.map(_.map(f)))

    def flatMap[B](f: A => FutOpt[B])(implicit ec: ExecutionContext): FutOpt[B] =
      copy(value.flatMap {
        case Some(a) => f(a).value
        case None => Future.successful(None)
      })
  }

  case class FutEith[A, B](value: Future[Either[A, B]]) {

    def map[C](f: B => C)(implicit ec: ExecutionContext): FutEith[A, C] =
      copy(value.map(_.map(f)))

    def flatMap[C](f: B => FutEith[A, C])(implicit ec: ExecutionContext): FutEith[A, C] =
      copy(value.flatMap {
        case Right(a) => f(a).value
        case Left(a) => Future.successful(Left(a))
      })
  }

  object TimeUtils {
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]X")

    def parse(timeStr: String): Instant = {
      Instant.from(timeFormatter.parse(timeStr))
    }
  }

}
