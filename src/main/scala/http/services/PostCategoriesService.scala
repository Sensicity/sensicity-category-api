package http
package services

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import cats.data.NonEmptyList
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._
import persistence.CategoryRepository

import scala.concurrent.ExecutionContext

/**
  * Service used for attaching categories to an specific identifier.
  */
trait PostCategoriesService extends ErrorAccumulatingCirceSupport {

  implicit def ec: ExecutionContext

  private[http] def categoryRepository: CategoryRepository

  @inline val postCategoriesRoute: Route = {
    decodeRequest {
      entity(as[PostCategoriesService.Input]) {
        input => {
          val query = categoryRepository.insertCategories(input.identifier, input.categories)
          onSuccess(query) {
            val outputMessage = {
              input.categories.toList match {
                case list if list.size > 1 =>
                  val categoriesStringRep = list.mkString("[", ", ", "]")
                  s"The categories $categoriesStringRep are now attached to '${input.identifier}'"
                case _ =>
                  s"The category '${input.categories.head}' is now attached to '${input.identifier}'"
              }
            }
            complete(models.Success(outputMessage).asJson.noSpaces)
          }
        }
      }
    }
  }
}

private object PostCategoriesService {

  case class Input(identifier: String, categories: NonEmptyList[String])

}
