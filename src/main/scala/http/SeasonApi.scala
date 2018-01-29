package http

import model.{ Season, TvShow }
import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import StatusCodes._
import HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import spray.json._
import akka.util.ByteString
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink

import scala.concurrent.duration._
import akka.stream.ThrottleMode
import exceptions.TooManyRequestsException

trait SeasonApi extends TmdbApi {
  
  
  def getDetails(tvShow: TvShow, season: Season): Future[Season] = {
    
    val uri = Uri("/3/tv/" + tvShow.id + "/season/" + season.number )
    val query = Query("api_key" -> apiKey)
    val request = HttpRequest(GET, uri = uri.withQuery(query))
    
    
    Source.single(request).via(client).mapAsync(1)( resp => resp match {
      
      case HttpResponse(OK, headers, entity, _) =>
        Unmarshal(entity).to[Season]
      case response @ HttpResponse(TooManyRequests, headers, _, _) => {
        val retryAfter = getRetryAfter(headers)
        response.discardEntityBytes()
        Future.failed(new TooManyRequestsException(retryAfter = retryAfter))
      }
      case _ => {
        resp.discardEntityBytes()
        Future.failed(new Exception("Unable to fetch season details"))
      }
    }).runWith(Sink.head)
    
  }
}