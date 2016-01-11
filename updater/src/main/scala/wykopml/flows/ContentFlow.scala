package wykopml.flows

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import wykopml.bo.{Comment, Wykop}
import wykopml.parsers.ContentParser

import scala.concurrent.ExecutionContextExecutor

trait ContentFlow {
  implicit def system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit def materializer: Materializer

  val contentFlow = Flow[(String, Wykop)].map {
    case (content, wykop) =>
      (ContentParser.parse(content, wykop), wykop)
  }

  val onlyCommentsFlow = Flow[(List[Comment], Wykop)].flatMapConcat {
    case (comments, _) =>
      Source(comments)
  }

}
