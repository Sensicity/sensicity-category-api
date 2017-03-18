package persistence

import java.time.Instant

import akka.actor.ActorSystem
import models.Categories
import org.specs2.mutable.Specification
import redis.RedisClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Specifications for @see [[CategoryRepository]]
  */
class CategoryRepositoryIntegrationSpec extends Specification {

  implicit val akkaSystem: ActorSystem = ActorSystem()

  private[this] val timeout = Duration.Inf

  sequential
  "CategoryRepositoryIntegrationSpec End to End tests" should {
    val dbName = s"CategoryRepositoryIntegrationSpec-${Instant.now()}"
    lazy val redisConnector = {
      val connector = RedisClient(name = dbName)
      Await.result(connector.flushdb(), timeout)
      connector
    }
    lazy val repository = new CategoryRepository(redisConnector)

    "Prerequisite: The connection with Redis was properly stabilised" in {
      implicit ec: ExecutionContext => {
        Await.result(redisConnector.ping(), timeout) must beEqualTo("PONG")
      }
    }
    "Prerequisite: The generated database is empty" in {
      implicit ec: ExecutionContext => {
        Await.result(redisConnector.dbsize(), timeout) must beEqualTo(0)
      }
    }
    val identifier1 = "ID_1"
    val category1 = "CARS"

    "Adding a category to a single user works as expected" in {
      implicit ec: ExecutionContext => {
        val isInserted: Future[Boolean] =
          repository
            .insertCategories(identifier1, category1)
            .map(_ => true)
            .recover { case _ => false }

        Await.result(isInserted, timeout) must beTrue
      }
    }
    "The inserted category is returned when asking for the identifier" in {
      implicit ec: ExecutionContext => {
        val queryResult: Future[Categories] =
          repository.findCategoriesByIdentifier(identifier1)

        Await.result(queryResult, timeout).categories must beEqualTo(Set(category1))
      }
    }
    val category2 = "ANIMALS"
    val category3 = "BOOKS"
    "Adding multiple categories works as expected" in {
      implicit ec: ExecutionContext => {
        val queryResult: Future[Boolean] =
          repository
            .insertCategories(identifier1, category2, category3)
            .map(_ => true)
            .recover { case _ => false }

        Await.result(queryResult, timeout) must beTrue
      }
    }
    "The inserted categories join the previous ones" in {
      implicit ec: ExecutionContext => {
        val queryResult: Future[Categories] =
          repository.findCategoriesByIdentifier(identifier1)

        val expectation: Set[String] = Set(category1, category2, category3)
        Await.result(queryResult, timeout).categories must beEqualTo(expectation)
      }
    }
    "Adding an already inserted category will not have any side-effect" in {
      implicit ec: ExecutionContext => {
        val hasSideEffects = {
          for {
            initialCategories <- repository.findCategoriesByIdentifier(identifier1)
            initialCount <- redisConnector.dbsize()
            _ <- repository.insertCategories(identifier1, category2)
            endCategories <- repository.findCategoriesByIdentifier(identifier1)
            endCount <- redisConnector.dbsize()
          } yield (initialCount !== endCount) || !initialCategories.equals(endCategories)
        }
        Await.result(hasSideEffects, timeout) must beFalse
      }
    }
    val category4 = "CRICKET"
    "Removing a non-existent category will not have any side-effect" in {
      implicit ec: ExecutionContext => {
        val hasSideEffects = {
          for {
            initialCategories <- repository.findCategoriesByIdentifier(identifier1)
            initialCount <- redisConnector.dbsize()
            _ <- repository.removeCategories(identifier1, category4)
            endCategories <- repository.findCategoriesByIdentifier(identifier1)
            endCount <- redisConnector.dbsize()
          } yield (initialCount !== endCount) || !initialCategories.equals(endCategories)
        }
        Await.result(hasSideEffects, timeout) must beFalse
      }
    }
    "Must work when inserting mixed inserted/non-inserted categories" in {
      implicit ec: ExecutionContext => {
        val isExpectedOutput = {
          for {
            initialCategories <- repository.findCategoriesByIdentifier(identifier1)
            _ <- repository.insertCategories(identifier1, category1, category4)
            endCategories <- repository.findCategoriesByIdentifier(identifier1)
            expectedCategories = Categories(initialCategories.categories + category4)
          } yield expectedCategories.equals(endCategories)
        }
        Await.result(isExpectedOutput, timeout) must beTrue
      }
    }
    val category5 = "FOOTBALL"
    "Must work when removing mixed inserted/non-inserted categories " in {
      implicit ec: ExecutionContext => {
        val isExpectedOutput = {
          for {
            initialCategories <- repository.findCategoriesByIdentifier(identifier1)
            _ <- repository.removeCategories(identifier1, category1, category5)
            endCategories <- repository.findCategoriesByIdentifier(identifier1)
            expectedCategories = Categories(initialCategories.categories - category1)
          } yield expectedCategories.equals(endCategories)
        }
        Await.result(isExpectedOutput, timeout) must beTrue
      }
    }
    val identifier2 = "ID_2"
    "Returns an empty List when asking the categories of an non-existent identifier" in {
      implicit ec: ExecutionContext => {
        val query = repository.findCategoriesByIdentifier(identifier2)
        Await.result(query, timeout).categories must beEqualTo(Set[String]())
      }
    }
    "Returns an empty List when asking the identifiers of an non-existent category" in {
      implicit ec: ExecutionContext => {
        val query = repository.findIdentifiersByCategories(category5)
        Await.result(query, timeout).categories must beEqualTo(Set[String]())
      }
    }
    "When inserting a second identifier the first one remains untouched" in {
      implicit ec: ExecutionContext => {
        val hasSideEffectsOnOtherIdentifier = {
          for {
            initialCategoriesIdentifier1 <- repository.findCategoriesByIdentifier(identifier1)
            _ <- repository.insertCategories(identifier2, category1, category2)
            endCategoriesIdentifier2 <- repository.findCategoriesByIdentifier(identifier1)
          } yield !initialCategoriesIdentifier1.equals(endCategoriesIdentifier2)
        }
        val secondIdentifierCategories =
          hasSideEffectsOnOtherIdentifier
            .flatMap(_ => repository.findCategoriesByIdentifier(identifier2))

        (Await.result(hasSideEffectsOnOtherIdentifier, timeout) must beFalse) &&
          (Await.result(secondIdentifierCategories, timeout).categories must beEqualTo(Set(category1, category2)))
      }
    }
    "Asking for the identifier from a category works as expected" in {
      implicit ec: ExecutionContext => {
        val queryCategory1 = repository.findIdentifiersByCategories(category1)
        val queryCategory2 = repository.findIdentifiersByCategories(category2)

        (Await.result(queryCategory1, timeout).categories must beEqualTo(Set(identifier2))) &&
          (Await.result(queryCategory2, timeout).categories must beEqualTo(Set(identifier1, identifier2)))
      }
    }
  }
}
