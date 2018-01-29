package model

case class TvShow(id: Long, name: String, voteAverage: Double, seasons: List[Season] = List(), cast: List[Member] = List())
