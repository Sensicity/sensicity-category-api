package config

import com.typesafe.config.ConfigFactory

/**
  * Contains the security token configuration. (Facade of `src/main/resources/redis.conf`)
  */
object SecurityTokenConfig {

  /**
    * Obtains the security token required on all the REST services petitions
    * if it is properly set by the user.
    */
  val securityToken: Option[String] =
    ConfigFactory.load("security.conf").getString("auth_token") match {
      case "null" => None
      case token => Some(token)
    }
}
