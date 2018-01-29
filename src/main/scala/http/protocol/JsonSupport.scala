package http.protocol

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import model.{TvShow, Season }
import model.Epsiode
import model.Member
import scala.collection.Seq
import scala.reflect.ClassManifestFactory.classType


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
    
  implicit val memberseFormat: RootJsonFormat[Member] =
    jsonFormat4(Member)
  
  implicit val episodeFormat: RootJsonFormat[Epsiode] =
    jsonFormat(Epsiode.apply, "id", "episode_number", "name", "vote_average")
    
  implicit object SeasomFormat extends RootJsonFormat[Season] {
     
    def write(c: Season) = JsObject(
      "id" -> JsNumber(c.id),
      "season_number" -> JsNumber(c.number)
    )
    
    def read(json: JsValue) = {
      val jsObject = json.asJsObject
      
      val season = jsObject.getFields("id", "season_number") match {
        case Seq(JsNumber(id), JsNumber(number)) => Season(id.toLong, number.toLong)
        case x => throw new DeserializationException("Id, name and season_number expected")
      }
      
      jsObject.getFields("episodes") match {
        case Seq(JsArray(episodes)) => 
          season.copy(episodes = episodes.map( _.convertTo[Epsiode]).toList)
        case _ => season
      }
    }
  }
  
  implicit object TvShowFormat extends RootJsonFormat[TvShow] {
     
    def write(c: TvShow) = JsObject(
      "id" -> JsNumber(c.id),
      "name" -> JsString(c.name),
      "vote_average" -> JsNumber(c.voteAverage)
    )
    
    def read(json: JsValue) = {
      val jsObject = json.asJsObject
      
      val tvShow = jsObject.getFields("id", "name", "vote_average") match {
        case Seq(JsNumber(id), JsString(name), JsNumber(voteAverage)) =>
          TvShow(id.toLong, name, voteAverage.toDouble )
        
        case x => throw new DeserializationException("Id, name and vote average expected")
        
      }
      
      jsObject.getFields("seasons") match {
        case Seq(JsArray(seasons)) => 
          tvShow.copy(seasons = seasons.map( _.convertTo[Season]).toList)
        
        case _ => tvShow
      }
    }
  }
  
  implicit object TvShowsFormat extends RootJsonFormat[List[TvShow]] {
  
    def write(c: List[TvShow]) = JsObject(
      "results" -> JsArray(c.map(_.toJson).toVector)
    )
    
    def read(json: JsValue) = {
      val jsObject = json.asJsObject
      
      jsObject.getFields("results") match {
        case Seq(JsArray(results)) => {
          results.map( _.convertTo[TvShow]).toList
        }
        
        case _ => throw new DeserializationException("Results expected")
      }
    }
  
  }
  
  implicit object castFormat extends RootJsonFormat[List[Member]] {
  
    def write(c: List[Member]) = JsObject(
      "cast" -> JsArray(c.map(_.toJson).toVector)
    )
    
    def read(json: JsValue) = {
      val jsObject = json.asJsObject
      
      jsObject.getFields("cast") match {
        case Seq(JsArray(cast)) => cast.map( _.convertTo[Member]).toList
        case _ => throw new DeserializationException("cast expected")
      }
    }
  
  }

}