
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
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
  override def redisConnector: RedisClient = RedisClient(name = "Sensicity-category-api")

  private[this] val httpInterface: String = "0.0.0.0"
  private[this] val httpPort : Int = 9090

  // Initializes the HTTP services
  println(s"Initializing REST services on: $httpInterface:$httpPort...")

  Http().bindAndHandle(
    handler = routes,
    interface = httpInterface,
    port = httpPort
  ).foreach(
    _ => println("REST services initialized successfully")
  )
}
