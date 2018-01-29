package http

import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import StatusCodes._
import HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import model.{ TvShow, Member }
import spray.json._
import akka.util.ByteString
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.scaladsl.{ Source, Sink }
import exceptions.TooManyRequestsException

trait TvShowApi extends TmdbApi{
  
  def getTopRated(page: Integer = 1): Future[List[TvShow]] = {
    val uri = Uri("/3/tv/top_rated")  
    val query = Query("api_key" -> apiKey,
    "language" -> "en",
    "page" -> page.toString)
    
    val request = HttpRequest(GET, uri = uri.withQuery(query))
   
    Source.single(request).via(client).mapAsync(1)( resp => resp match {
      case HttpResponse(a, headers, entity, _) =>
        Unmarshal(resp.entity).to[List[TvShow]]
      case response @ HttpResponse(TooManyRequests, headers, _, _) => {
        val retryAfter = getRetryAfter(headers)
        response.discardEntityBytes()
        Future.failed(new TooManyRequestsException(retryAfter = retryAfter))
      }
      case response => {
        response.discardEntityBytes()
        Future.failed(new Exception("Unable to fetch tv show details"))
      }
    }).runWith(Sink.head)
  }
    
  def getDetails(tvShow: TvShow): Future[TvShow] = {
    val uri = Uri(s"/3/tv/${tvShow.id}")
    val query = Query("api_key" -> apiKey)
    val request = HttpRequest(GET, uri = uri.withQuery(query))
   
    Source.single(request).via(client).mapAsync(1)( resp => resp match {
      case HttpResponse(a, headers, entity, _) =>
        Unmarshal(entity).to[TvShow]
      case response @ HttpResponse(TooManyRequests, headers, _, _) => {
        val retryAfter = getRetryAfter(headers)
        response.discardEntityBytes()
        Future.failed(new TooManyRequestsException(retryAfter = retryAfter))
      }
      case response => {
        response.discardEntityBytes()
        Future.failed(new Exception("Unable to fetch tv show details"))
      }
    }).runWith(Sink.head)
  }
  
  def getCast(tvShow: TvShow): Future[List[Member]] = {
    val uri = Uri("/3/tv/" + tvShow.id + "/credits")
    val query = Query("api_key" -> apiKey)
    val request = HttpRequest(GET, uri = uri.withQuery(query))
    
    Source.single(request).via(client).mapAsync(1)( resp => resp match {
      case HttpResponse(a, headers, entity, _) =>
        Unmarshal(entity).to[List[Member]]
        
      case response @ HttpResponse(TooManyRequests, headers, _, _) => {
        val retryAfter = getRetryAfter(headers)
        response.discardEntityBytes()
        Future.failed(new TooManyRequestsException(retryAfter = retryAfter))
      }
      case response => {
        response.discardEntityBytes()
        Future.failed(new Exception("Unable to fetch cast"))
      }
    }).runWith(Sink.head)
  }
    
}