package http

import utils.AkkaExecutor
import protocol.JsonSupport
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import utils.Config
import akka.http.scaladsl.Http
import scala.util.Success
import akka.http.scaladsl.model.HttpHeader
import scala.util.Try

trait TmdbApi extends AkkaExecutor with Config with JsonSupport {
  
  override implicit val system = ActorSystem()
  override implicit val materializer = ActorMaterializer()
  override implicit val executionContext = system.dispatcher
  implicit val executor = scala.concurrent.ExecutionContext
  
  val client = Http().outgoingConnectionHttps(host = tmdbUrl)
   
  def getRetryAfter(headers: Seq[HttpHeader]) = {
    headers.find( _.name == "Retry-After") match {
      case Some(header) => {
        Try(header.value().toInt) match {
          case Success(value) => value
          case _ => 0
        }
      }
      case _ => 0
    }
  }
}