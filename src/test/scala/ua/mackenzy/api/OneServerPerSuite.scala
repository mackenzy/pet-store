package ua.mackenzy.api

import java.lang.System.nanoTime
import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.server
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.google.inject._
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import ua.mackenzy.api.module.{AkkaModule, Handlers, RoutesModule}
import ua.mackenzy.api.util._

import scala.concurrent.ExecutionContext

trait OneServerPerSuite extends Handlers {

  val system: ActorSystem
  val materializer: ActorMaterializer

  lazy val injector: Injector = createInjector
  lazy val dbManager: TestDbManager = initDbManager
  lazy val route: server.Route = Route.seal(routes)
  lazy implicit val impClock: Clock = clock

  def createInjector: Injector =
    Guice.createInjector(
      Stage.PRODUCTION,
      new AkkaModule(clock, system, materializer),
      new RoutesModule(),
      new TestDbModule()
    )

  def clock: Clock =
    Clock.systemUTC

  def routes: Route =
    injector.getInstance(Key.get(new TypeLiteral[Route]() {}))

  def initDbManager: TestDbManager =
    injector.getInstance(classOf[TestDbManager])

  def resetAndInitData: Unit = {
    dbManager.dropAndInitDb
    ()
  }

}

class TestDbModule extends AbstractModule {

  private val config = ConfigFactory.parseString(
    s"""sqllite {
       |  driver = "slick.driver.SQLiteDriver$$"
       |  db {
       |    url = "jdbc:sqlite:target/sqlite_${nanoTime}_${hashCode}.db"
       |    driver = org.sqlite.JDBC
       |    connectionPool = disabled
       |  }
       |}
       """.stripMargin)

  @Provides
  @Singleton
  def db(): DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig[JdbcProfile]("sqllite", config)

}

@Singleton
class TestDbManager @Inject()(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {

  def dropAndInitDb: Unit = {
    import dbConfig.profile.api._

    val queries = DBIO.seq(
      sqlu"""DROP TABLE IF EXISTS `users`""",
      sqlu"""DROP TABLE IF EXISTS `pets_categories`""",
      sqlu"""DROP TABLE IF EXISTS `pets`""",
      sqlu"""DROP TABLE IF EXISTS `orders_status`""",
      sqlu"""DROP TABLE IF EXISTS `orders`""",
      sqlu"""CREATE TABLE `users`(
                 `id`   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                 `name` TEXT    NOT NULL
               )""",
      sqlu"""CREATE TABLE `pets_categories`
               (
                 `id`    INTEGER NOT NULL,
                 `value` TEXT    NOT NULL,
                 PRIMARY KEY (`id`)
               )""",
      sqlu"""CREATE TABLE `pets`(
                 `id`          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                 `category_id` INTEGER NOT NULL DEFAULT 1,
                 `name`        TEXT    NOT NULL,
                 `photo_url`   TEXT,
                 FOREIGN KEY (`category_id`) REFERENCES `pets_categories` (`id`)
               )""",
      sqlu"""CREATE TABLE `orders_status`(
                 `id`    INTEGER NOT NULL,
                 `value` TEXT    NOT NULL,
                 PRIMARY KEY (`id`)
               )""",
      sqlu"""CREATE TABLE `orders`(
                 `id`        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                 `status_id` INTEGER NOT NULL,
                 `pet_id`    INTEGER NOT NULL,
                 `user_id`   INTEGER NOT NULL,
                 FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                 FOREIGN KEY (`status_id`) REFERENCES `orders_status` (`id`),
                 FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`)
               )""",

      sqlu"""INSERT INTO orders_status(id, value) VALUES (1, 'placed')""",
      sqlu"""INSERT INTO orders_status(id, value) VALUES (2, 'processing')""",
      sqlu"""INSERT INTO orders_status(id, value) VALUES (3, 'completed')""",

      sqlu"""INSERT INTO pets_categories(id, value) VALUES (1, 'cat')""",
      sqlu"""INSERT INTO pets_categories(id, value) VALUES (2, 'dog')"""
    )

    dbConfig.db.run(queries).await
  }
}
