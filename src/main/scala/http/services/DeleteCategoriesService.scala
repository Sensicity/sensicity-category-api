package http.services

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import cats.data.NonEmptyList
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._
import persistence.CategoryRepository

import scala.concurrent.ExecutionContext

/**
  * Service used for removing categories from an specific identifier.
  */
trait DeleteCategoriesService extends ErrorAccumulatingCirceSupport {

  implicit def ec: ExecutionContext

  private[http] def categoryRepository: CategoryRepository

  @inline val deleteCategoriesRoute: Route = {
    decodeRequest {
      entity(as[DeleteCategoriesService.Input]) {
        input => {
          val query = categoryRepository.insertCategories(input.identifier, input.categories)
          onSuccess(query) {
            val outputMessage = {
              input.categories.toList match {
                case list if list.size > 1 =>
                  val categoriesStringRep = list.mkString("[", ", ", "]")
                  s"The categories $categoriesStringRep are not longer attached to '${input.identifier}'"
                case _ =>
                  s"The category '${input.categories.head}' are not longer attached to '${input.identifier}'"
              }
            }
            complete(models.Success(outputMessage).asJson.noSpaces)
          }
        }
      }
    }
  }
}

object DeleteCategoriesService {

  case class Input(identifier: String, categories: NonEmptyList[String])

}
