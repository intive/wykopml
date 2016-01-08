package wykopml.flows

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.scaladsl._
import akka.stream.{Materializer, ThrottleMode}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

trait FetcherFlow {

  private val logger = LoggerFactory.getLogger(getClass)

  implicit def system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit def materializer: Materializer

  private def wykopConnectionFlow[T] = Http().cachedHostConnectionPool[T]("www.wykop.pl", 80)

  val safeStringToUri: (String) => Uri = (str) => {
    val parts = str.split('/')
    val path = parts.foldLeft[Uri.Path](Uri.Path.Empty)(_ / _)
    Uri(path./("").toString().stripPrefix("/"))
  }

  def contentFetcherFlow = Flow[HttpRequest]
    .map { r => (r, ()) }.via(fetcherFlow[String, Unit]()).map(_._1)

  def stringToGetRequestFlow = Flow[String].map {
    url =>
      (HttpRequest(HttpMethods.GET, safeStringToUri(url)), url)
  }

  def objToGetRequestFlow[T](fn: (T) => Uri) = Flow[T].map {
    obj: T =>
      (HttpRequest(HttpMethods.GET, fn(obj)), obj)
  }

  def fetcherFlow[Output, T]()(implicit um: Unmarshaller[HttpResponse, Output]) = Flow[(HttpRequest, T)]
    .throttle(5, 1.second, 2, ThrottleMode.Shaping)
    .map {
      req =>
        {
          logger.info(s"Will retrieve ${req._1.uri}")
          req
        }
    }
    .via(wykopConnectionFlow)
    .mapAsync(8) {
      case (resTry, t) =>
        resTry match {
          case Success(response) =>
            Unmarshal(response).to[Output].map((_, t))
          case Failure(ex) => {
            logger.error("Unable to retrieve", ex)
            Future.failed(ex)
          }
        }
    }

}
