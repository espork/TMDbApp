package utils

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext

trait AkkaExecutor {
  implicit def system: ActorSystem
  implicit def executionContext: ExecutionContext
  implicit def materializer: ActorMaterializer
}