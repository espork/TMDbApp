package utils


import com.typesafe.config.ConfigFactory


trait Config {
  private val config = ConfigFactory.load()
  private val tmdb = config.getConfig("tmdb")
  
  val tmdbUrl = tmdb.getString("url")
  val apiKey = tmdb.getString("apiKey")
}