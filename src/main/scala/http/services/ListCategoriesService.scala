package http.services

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._
import persistence.CategoryRepository

import scala.concurrent.ExecutionContext

/**
  * Service used for querying for the categories from an identifier.
  */
trait ListCategoriesService extends ErrorAccumulatingCirceSupport {

  implicit def ec: ExecutionContext

  private[http] def categoryRepository: CategoryRepository

  @inline val listCategoriesRoute: Route = {
    onSuccess(
      categoryRepository.findAllInsertedCategories()
    ) {
      categories =>
        complete(
          categories.asJson.noSpaces
        )
    }
  }
}
