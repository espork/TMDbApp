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
  
  val client = Http().outgoingConnectionHttps(host = tmdbUrl)
   
  def getRetryAfter(headers: Seq[HttpHeader]) = 
    headers.find( _.name == "Retry-After").flatMap(header => Try(header.value.toInt)
        .toOption)
        .getOrElse(0)
  
}