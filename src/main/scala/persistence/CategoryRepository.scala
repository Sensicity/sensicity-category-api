package persistence

import cats.data.NonEmptyList
import models.{Categories, CategoryIdentifiers}
import redis.RedisClient
import redis.commands.TransactionBuilder

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

/**
  * Facade against a Redis Database used for storing the categories from an element.
  */
class CategoryRepository(private val redisClient: RedisClient) {

  @inline
  private[this] def categoryKey(identifier: String): String = s"C|$identifier"

  @inline
  private[this] def identifierKey(identifier: String): String = s"I|$identifier"

  @inline
  private[this] val categoryListKey: String = "categoryList"

  /**
    * Inserts a category into the Redis Database assigned to the input identifier.
    *
    * @param identifier      from the user owning the device.
    * @param category        being inserted into the Redis database.
    * @param otherCategories being inserted into the Redis database.
    * @return a [[scala.util.Success]] if the insertion is successful - [[scala.util.Failure]] otherwise
    *         as soon as the petition is successfully processed.
    */
  def insertCategories(identifier: String,
                       category: String,
                       otherCategories: String*)(implicit ec: ExecutionContext): Future[Unit] = {

    val categoriesToAdd = NonEmptyList.fromListUnsafe((otherCategories :+ category).toList)
    insertCategories(identifier, categoriesToAdd)
  }

  /**
    * Inserts a category into the Redis Database assigned to the input identifier.
    *
    * @param identifier from the user owning the device.
    * @param categories being inserted into the Redis database.
    * @return a [[scala.util.Success]] if the insertion is successful - [[scala.util.Failure]] otherwise
    *         as soon as the petition is successfully processed.
    */
  def insertCategories(identifier: String,
                       categories: NonEmptyList[String])(implicit ec: ExecutionContext): Future[Unit] = {

    val transaction: TransactionBuilder = redisClient.transaction()

    @inline val categoriesIdentifier: NonEmptyList[String] = categories.map(categoryKey)
    @inline val persistedIdentifier: String = identifierKey(identifier)

    // Adds categories to the elements by category
    categoriesIdentifier.map(transaction.sadd[String](_, identifier))

    // Adds categories to the category List
    categories.map(transaction.sadd[String](categoryListKey, _))

    // Adds category to identifier
    transaction.sadd[String](persistedIdentifier, categories.toList: _*)

    transaction.exec().map(_ => Unit)
  }

  /**
    * Removes a set of categories assigned to an identifier.
    *
    * @param identifier      which categories are being removed.
    * @param category        being removed from the database.
    * @param otherCategories that are also being removed from the database.
    * @return a [[scala.util.Success]] if the removal was successful - [[scala.util.Failure]]
    *         otherwise as soon as the petition is successfully processed.
    */
  def removeCategories(identifier: String,
                       category: String,
                       otherCategories: String*)(implicit ec: ExecutionContext): Future[Unit] = {

    val categoriesToRemove = NonEmptyList.fromListUnsafe((otherCategories :+ category).toList)
    removeCategories(identifier, categoriesToRemove)
  }

  /**
    * Removes a set of categories assigned to an identifier.
    *
    * @param identifier which categories are being removed.
    * @param categories to be removed.
    * @return a [[scala.util.Success]] if the removal was successful - [[scala.util.Failure]]
    *         otherwise as soon as the petition is successfully processed.
    */
  def removeCategories(identifier: String,
                       categories: NonEmptyList[String])(implicit ec: ExecutionContext): Future[Unit] = {

    val transaction: TransactionBuilder = redisClient.transaction()

    @inline val categoriesIdentifier: NonEmptyList[String] = categories.map(categoryKey)
    @inline val persistedIdentifier: String = identifierKey(identifier)

    // If one of the categories is from the last identifier assigned in the category list,
    // removes the list from the category list.
    Future.sequence(
      categories.map {
        categoryToRemove => {
          findIdentifiersByCategory(categoryToRemove).map(_.categories.toSeq).map {
            case Seq(insertedElement) if insertedElement.equals(identifier) =>
              transaction.srem(categoryListKey, categoryToRemove)
            case _ => Unit
          }
        }
      }.toList
    ).map(
      // Removes the category from the list of categories from an identifier.
      _ => categoriesIdentifier.map(transaction.srem[String](_, identifier))
    ).map(
      // Removes the identifier from the list of category identifiers.
      _ => transaction.srem[String](persistedIdentifier, categories.toList: _*)
    ).flatMap(
      // Executes the transaction.
      _ => transaction.exec().map(_ => Unit)
    )
  }

  /**
    * Obtains a [[Set]] with all the categories belonging to the input identifier.
    *
    * @param identifier that we want to use for retrieving the categories.
    * @return a [[Set]] with all the categories assigned to the input identifier.
    */
  def findCategoriesByIdentifier(identifier: String)
                                (implicit ec: ExecutionContext): Future[Categories] =
    redisClient.smembers[String](identifierKey(identifier)).map(_.toSet).map(Categories)

  /**
    * Obtains a [[Set]] with all the identifiers that have the input category assigned.
    *
    * @param category that we will use for obtaining the identifiers.
    * @return a [[Set]] with all the identifiers having the input category.
    */
  def findIdentifiersByCategory(category: String)
                               (implicit ec: ExecutionContext): Future[CategoryIdentifiers] =
    redisClient.smembers[String](categoryKey(category)).map(_.toSet).map(CategoryIdentifiers)

  /**
    * Obtains a [[Set]] with all the category identifiers assigned to any user in the database.
    *
    * @return a [[Set]] with all the category identifiers.
    */
  def findAllInsertedCategories()(implicit ec: ExecutionContext): Future[Categories] =
    redisClient.smembers[String](categoryListKey).map(_.toSet).map(Categories)

}
