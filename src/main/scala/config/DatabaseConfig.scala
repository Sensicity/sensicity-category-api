package config

import com.typesafe.config.ConfigFactory

/**
  * Set of configurations used when connecting to the Redis database.
  * (Facade of `src/main/resources/redis.conf`)
  */
object DatabaseConfig {

  /**
    * Obtains the location of the Redis database.
    */
  val redisHost: String = ConfigFactory.load("redis.conf").getString("host")

  /**
    * Obtains the name of the Redis database.
    */
  val redisDbName: String = ConfigFactory.load("redis.conf").getString("database_name")

}
