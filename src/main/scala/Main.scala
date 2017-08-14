
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import config.DatabaseConfig
import http.ServiceRouter
import redis.RedisClient

import scala.concurrent.ExecutionContext

/**
  * Application entry point.
  */
object Main extends App with ServiceRouter {

  override implicit val system: ActorSystem = ActorSystem()
  override implicit val ec: ExecutionContext = system.dispatcher
  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  // Initialize Redis database

  override val redisConnector: RedisClient =
    RedisClient(host = DatabaseConfig.redisHost, name = DatabaseConfig.redisDbName)

  println(
    s"Connected to Redis database '${DatabaseConfig.redisDbName}' at '${DatabaseConfig.redisHost}'"
  )

  // Initializes the HTTP services

  private[this] def httpInterface: String = "0.0.0.0"
  private[this] def httpPort: Int = 9090

  println(s"Initializing REST services on: $httpInterface:$httpPort...")

  Http().bindAndHandle(
    handler = routes,
    interface = httpInterface,
    port = httpPort
  ).foreach(
    _ => println("REST services initialized successfully")
  )
}
