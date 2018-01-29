package exceptions

case class TooManyRequestsException(message: String = "Too Many Requests", retryAfter: Int) extends Exception(message)
  
