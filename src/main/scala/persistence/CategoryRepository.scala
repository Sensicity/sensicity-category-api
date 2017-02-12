package persistence

import cats.data.NonEmptyList
import models.{Categories, CategoryIdentifiers}
import redis.commands.TransactionBuilder
import redis.{ByteStringDeserializer, ByteStringSerializer, RedisClient}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Facade against a Redis Database used for storing the categories from an element.
  */
class CategoryRepository(private val redisClient: RedisClient) {

  @inline
  private[this] def categoryKey(identifier: String): String = s"C|$identifier"

  @inline
  private[this] def identifierKey(identifier: String): String = s"I|$identifier"

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

    categoriesIdentifier.map(transaction.sadd[String](_, identifier))
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
                       categories: NonEmptyList[String])(implicit ec : ExecutionContext) : Future[Unit] = {

    val transaction: TransactionBuilder = redisClient.transaction()

    @inline val categoriesIdentifier: NonEmptyList[String] = categories.map(categoryKey)
    @inline val persistedIdentifier: String = identifierKey(identifier)

    categoriesIdentifier.map(transaction.srem[String](_, identifier))
    transaction.srem[String](persistedIdentifier, categories.toList: _*)

    transaction.exec().map(_ => Unit)
  }

  /**
    * Obtains a [[Set]] with all the categories belonging to the input identifier.
    *
    * @param identifier that we want to use for retrieving the categories.
    * @return a [[Set]] with all the categories.
    */
  def findCategoriesByIdentifier(identifier: String)
                                (implicit ec : ExecutionContext): Future[Categories] =
   redisClient.smembers[String](identifierKey(identifier)).map(_.toSet).map(Categories)

  /**
    * Obtains a [[Set]] with all the identifiers that have the input category assigned.
    *
    * @param category that we will use for obtaining the identifiers.
    * @return a [[Set]] with all the category identifiers.
    */
  def findIdentifiersByCategories(category: String)
                                 (implicit ec : ExecutionContext): Future[CategoryIdentifiers] =
    redisClient.smembers[String](categoryKey(category)).map(_.toSet).map(CategoryIdentifiers)

}
