package wykopml.storage

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import wykopml.bo.{Vote, Wykop}
import wykopml.flows.FetcherFlow
import wykopml.storage.VotersFlow.VoterUrl

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

trait VotersFlow {
  self: FetcherFlow =>
  implicit def system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit def materializer: Materializer

  def voterUrlsFromWykopFlow = Flow[Wykop].flatMapConcat {
    wykop =>
      Source(
        List(VoterUrl(wykop.id, wykop.upVotersUrl, true), VoterUrl(wykop.id, wykop.downVotersUrl, false))
      )
  }

  val Extractor = """   title=\\"([^\\]*)\\".*datetime=\\"([^\\]*)\\"   """.trim.r

  def votersFlow = {
    Flow[VoterUrl].map {
      voterUrl =>
        (HttpRequest(HttpMethods.GET, voterUrl.url), voterUrl)
    }.via(fetcherFlow[String, VoterUrl]())
      .flatMapConcat {
        case (content, voterUrl) =>
          val result = Try {
            Source.apply(
              content.split("usercard").flatMap {
              part =>
                Extractor.findAllMatchIn(part).map[Vote] {
                  m => Vote(voterUrl.wykopId, m.group(1), voterUrl.isUp, LocalDateTime.parse(m.group(2), DateTimeFormatter.ISO_DATE_TIME))
                }
            }.toList
            )
          }
          if (result.isFailure) println(result)
          result.get
      }
  }

}

object VotersFlow {

  case class VoterUrl(wykopId: Int, url: String, isUp: Boolean)

}