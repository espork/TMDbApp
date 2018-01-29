package service

import http.{ TvShowApi, SeasonApi, PeopleApi }
import model.{ TvShow, Member, Season, Epsiode }
import scala.concurrent.Future
import scala.util.{ Success, Failure }
import exceptions.TooManyRequestsException
import java.util.concurrent.TimeUnit

trait TvShowService extends TvShowApi with SeasonApi with PeopleApi {
  
  def getTop(n: Integer) = getTopRated().map( _ take n)
  
  def getTvShowInfosAndCast(tvShow: TvShow) = {
    val tvShowWithDetails = getTvShowDetails(tvShow)
    val castWithDetails =  getCast(tvShow).flatMap(getCastDetails)
    tvShowWithDetails zip castWithDetails
  }
  
  //Backoff strategy -- The api accepts only 40 requests every 10 seconds
  private def getAcotorDetail (actor: Member,  retryAfter: Int = 0):Future[Member] = {
    if(retryAfter > 0) TimeUnit.SECONDS.sleep(retryAfter)
    getDetail(actor).recoverWith {
      case ex: TooManyRequestsException => getAcotorDetail(actor,ex.retryAfter)
    }
  }
  
  //Backoff strategy
  private def getSeasonDetail(tvShow: TvShow, season: Season, retryAfter: Int = 0): Future[Season] = {
    if(retryAfter > 0) TimeUnit.SECONDS.sleep(retryAfter)
      getDetails(tvShow, season).recoverWith{
        case ex: TooManyRequestsException => getSeasonDetail(tvShow,season, ex.retryAfter)
      }
  }
  
  private def getCastDetails(cast: List[Member]) = {
    val castWithDetails = cast.map(getAcotorDetail(_)) 
    Future.foldLeft(castWithDetails)(List.empty[Member]){ (acc, actor) => actor :: acc }
  }

  private def getSeasonsDetails(tvShow: TvShow): Future[List[Season]] = {
    val seasons = tvShow.seasons.map(getSeasonDetail(tvShow, _))
    Future.foldLeft(seasons)(List.empty[Season]){ (acc, season) => season :: acc }
  }
  
  private def getTvShowDetails(tvShow: TvShow): Future[TvShow] = {
    for {
      tvShowWithSeaons <-  getDetails(tvShow).map( t => tvShow.copy(seasons = t.seasons))
      seasonsWithEpisodes <- getSeasonsDetails(tvShowWithSeaons)
    } yield tvShowWithSeaons.copy(seasons = seasonsWithEpisodes)
  }
  
}