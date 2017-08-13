package http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import http.services._
import persistence.CategoryRepository
import redis.RedisClient

import scala.concurrent.ExecutionContext

trait ServiceRouter
  extends ErrorAccumulatingCirceSupport
    with PostCategoriesService
    with DeleteCategoriesService
    with GetCategoriesService
    with GetIdentifiersFromCategoryService
    with ListCategoriesService {

  /**
    * Akka Actor system
    */
  implicit val system: ActorSystem

  /**
    * Akka Streams Materializer
    */
  implicit val materializer: Materializer

  /**
    * Execution context Executor
    */
  implicit def ec: ExecutionContext

  /**
    * Client used for connecting to the database
    */
  def redisConnector: RedisClient

  /**
    * Connector to the category repository.
    */
  private[http] lazy val categoryRepository = new CategoryRepository(redisConnector)

  /**
    * [[RejectionHandler]] used for improving method rejections.
    */
  implicit def rejectionHandler: RejectionHandler = {
    RejectionHandler.newBuilder().handleAll[MethodRejection] {
      rejections =>
        val methods = rejections.map(_.supported)
        lazy val names = methods.map(_.name).mkString(", ")

        respondWithHeader(Allow(methods)) {
          options {
            complete(s"Supported methods -> $names.")
          } ~ {
            complete(
              HttpResponse(
                status = MethodNotAllowed,
                entity = s"HTTP method not allowed, supported methods: $names"
              )
            )
          }
        }
    }.result()
  }

  /**
    * Published API
    */
  val routes: Route = {
    cors() {
      pathPrefix("categories") {
        post {
          postCategoriesRoute
        } ~ delete {
          deleteCategoriesRoute
        } ~ get {
          getCategoriesRoute
        } ~ path("list") {
          get {
            listCategoriesRoute
          }
        }
      } ~ path("identifiers") {
        get {
          getIdentifiersFromCategory
        }
      }
    }
  }
}
