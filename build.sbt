name := "TMDbApp"

version := "0.1.0"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-http"   % "10.1.0-RC1",
	"com.typesafe.akka" %% "akka-actor" % "2.5.9",
	"com.typesafe.akka" %% "akka-stream" % "2.5.8",
	"io.spray" %%  "spray-json" % "1.3.3",
	"com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0-RC1",
	"com.typesafe" % "config" % "1.3.1")