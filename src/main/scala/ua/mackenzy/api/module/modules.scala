package ua.mackenzy.api.module

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.google.inject._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import ua.mackenzy.api.util._

import scala.concurrent.ExecutionContext

class AkkaModule(
                  clock: Clock,
                  system: ActorSystem,
                  materializer: Materializer
                ) extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(clock)
    bind(classOf[ActorSystem]).toInstance(system)
    bind(classOf[Materializer]).toInstance(materializer)
    bind(classOf[ExecutionContext]).toInstance(system.dispatcher)
  }
}

class DbModule extends AbstractModule {

  @Provides
  @Singleton
  def db(): DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig[JdbcProfile]("sqllite")

}

class AkkaWebServerModule extends AbstractModule with Handlers {

  @Inject
  @Provides
  @Singleton
  def webServer(routes: Route)(implicit system: ActorSystem, m: Materializer, ec: ExecutionContext, clock: Clock): ServerBinding =
    Http()
      .bindAndHandle(routes, "localhost", 8080)
      .await
}
