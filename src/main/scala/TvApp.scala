package main.scala

import scala.concurrent.Future
import scala.util.{Failure, Success}
import service.TvShowService
import model.{ TvShow, Season, Epsiode, Member }

object TvApp extends App with TvShowService {
  
  getTop(10).onComplete {
    case Success(top10) => displayInfos(top10)
    case Failure(ex) => println(ex)
  }
  
  def displayInfos(tvShows: List[TvShow]): Unit = {
    tvShows match {
      case head::xs => {
        val future = getTvShowInfosAndCast(head)
        future.onComplete {
          case Success((tvShow, s)) => {
            print(tvShow, s)
            displayInfos(xs)  
          }
          case Failure(ex) => println(ex)
           
        }
      }
      case Nil => {
        println("Finished !")
        system.terminate()
      }
    }
  }
  
  def print(tvShow: TvShow, cast: List[Member]) = {
    println(s"${tvShow.name} : ${tvShow.voteAverage}")
    println("------ Top 10 Episodes -----")
    top10Episodes(tvShow).map( e => s"${e.name} : ${e.voteAverage}").foreach(println)
    println("------ Top 10 most popular actors -----")
    top10PopularActors(cast).map( a => s"${a.name} : ${a.popularity.get}").foreach(println)
    println("------------\n")
  }
   
  def top10Episodes(tvShow : TvShow): List[Epsiode] = {
    val allEpisodes = for {
      season <- tvShow.seasons
      episode <- season.episodes
    } yield episode
    allEpisodes.sortBy(- _.voteAverage).take(10)
  }
  
  def top10PopularActors(cast: List[Member]): List[Member] = {
    def popularity(actor: Member) = actor.popularity.getOrElse(0.0)
    cast.sortBy(- popularity(_)).take(10)
  }
  
}
