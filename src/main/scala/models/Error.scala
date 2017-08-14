package models

/**
  * Error message to be returned to the user.
  * @param code of the error message.
  * @param message with a proper description of the error.
  */
case class Error(code : String, message : String) {

  /**
    * Returns the class serialized as a trimmed JSON `String`.
    */
  def asJsonString: String =
    "{\"code\":\"" + code + "\"message\":\"" + message + "\"}"

}
