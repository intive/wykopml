package wykopml.flows

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl._
import wykopml.parsers.IndexParser

import scala.concurrent.ExecutionContextExecutor

trait IndexParserFlow {
  implicit def system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit def materializer: Materializer

  def indexParserFlow(isMainPage: Boolean) = {
    Flow[String].flatMapConcat {
      content =>
        Source(
          IndexParser.parseIndex(content, isMainPage)
        )
    }

  }

}
