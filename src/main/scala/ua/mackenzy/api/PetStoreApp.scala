package ua.mackenzy.api

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import ua.mackenzy.api.module.{RoutesModule, AkkaModule, AkkaWebServerModule, DbModule}

import scala.util.Try
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object PetStoreApp {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val system: ActorSystem = ActorSystem("api-test-akka-system")
    val materializer: ActorMaterializer = ActorMaterializer()(system)

    Try(launch(system, materializer))
      .recover {
        case t: Throwable =>
          log.error(s"PetStoreApp could not start: '${t.getMessage}'", t)
          system.terminate()
      }
  }

  def launch(system: ActorSystem, materializer: Materializer): Unit = {

    val injector = Guice.createInjector(
      Stage.PRODUCTION,
      new AkkaModule(Clock.systemUTC, system, materializer),
      new RoutesModule(),
      new AkkaWebServerModule(),
      new DbModule()
    )

    val bound = injector.getInstance(classOf[ServerBinding])
    log.info("Server started at http://{}:{}", bound.localAddress.getHostString, bound.localAddress.getPort)

    val db = injector.getInstance(Key.get(new TypeLiteral[DatabaseConfig[JdbcProfile]]() {})).db

    sys.ShutdownHookThread {
      log.info("shutting down")
      system.terminate()
      db.close()
    }
  }
}
