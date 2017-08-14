package http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import cats.implicits._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import config.SecurityTokenConfig
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import http.services._
import models.Error
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
    * Error message returned when a token authorization is not valid.
    */
  private[this] val unauthorizedErrorMessage: String =
    Error(
      code = "UNAUTHORIZED",
      message = "The security token is not valid"
    ).asJsonString

  /**
    * [[RejectionHandler]] used for improving method rejections.
    */
  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case MissingHeaderRejection("auth_token") =>
          complete(
            HttpResponse(
              status = BadRequest,
              entity = Error(
                code = "AUTH_TOKEN_REQUIRED",
                message = "Authentication token is required"
              ).asJsonString
            )
          )
      }.handleAll[MethodRejection] {
      rejections =>
        val methods = rejections.map(_.supported)
        lazy val names = methods.map(_.name).mkString(", ")

        respondWithHeader(
          Allow(methods)) {
          complete(
            HttpResponse(
              status = MethodNotAllowed,
              entity =
                Error(
                  code = "METHOD_NOT_ALLOWED",
                  message = s"HTTP method not allowed, supported methods: $names"
                ).asJsonString
            )
          )
        }
    }.result()

  private[this] def services =
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

  /**
    * Published API
    */
  val routes: Route =
    SecurityTokenConfig.securityToken match {
      case Some(token) =>
        // If the security token is set, check the petition token
        // is properly set before processing the request.
        cors() {
          headerValueByName("auth_token") {
            headerValue =>
              if (headerValue === token) {
                services
              } else {
                complete(
                  HttpResponse(
                    status = Unauthorized,
                    entity = unauthorizedErrorMessage
                  )
                )
              }
          }
        }
      case _ => cors() {
        services
      }
    }
}
