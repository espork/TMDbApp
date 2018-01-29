package http

import model.Member
import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import StatusCodes._
import HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.scaladsl.{ Source, Sink }
import exceptions.TooManyRequestsException

trait PeopleApi extends TmdbApi {
  
  def getDetail(person: Member): Future[Member] = {
    val uri = Uri("/3/person/" + person.id)
    val query = Query("api_key" -> apiKey)
    val request = HttpRequest(GET, uri = uri.withQuery(query))
      
    Source.single(request).via(client).mapAsync(1)( resp => resp match {
      case HttpResponse(OK, headers, entity, _) => Unmarshal(entity).to[Member]
      case response @ HttpResponse(TooManyRequests, headers, _, _) => {
        val retryAfter = getRetryAfter(headers)
        response.discardEntityBytes()
        Future.failed(new TooManyRequestsException(retryAfter = retryAfter))
      }
      case _ => {
        resp.discardEntityBytes()
        Future.failed(new Exception("Unable to fetch person details"))
      }
    }).runWith(Sink.head)
  }
}