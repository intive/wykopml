package wykopml

import java.util.logging.Logger

import akka.actor._
import akka.contrib.jul.JavaLoggingAdapter
import akka.event.LoggingAdapter
import akka.http.scaladsl.model._
import akka.stream._
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import wykopml.bo._
import wykopml.flows.{ContentFlow, FetcherFlow, IndexParserFlow}
import wykopml.storage.{VotersFlow, WykopDatabase}

import scala.concurrent.ExecutionContextExecutor

case object UpdatePipeline extends App with StrictLogging with FetcherFlow with IndexParserFlow with VotersFlow with ContentFlow {
  implicit val session = WykopDatabase.session
  implicit val keyspace = WykopDatabase.space

  WykopDatabase.init()

  val config = ConfigFactory.load()

  override implicit val system = ActorSystem("wykopml", config)
  override implicit val executor: ExecutionContextExecutor = system.dispatcher
  override implicit val materializer: Materializer = ActorMaterializer()

  implicit val loggingAdapter: LoggingAdapter = new JavaLoggingAdapter {
    override def logger: Logger = java.util.logging.Logger.getLogger("logger")
  }

  def indexPageUrl(page: Int) = s"/strona/$page/"

  def indexIncomingPageUrl(page: Int) = s"/wykopalisko/strona/$page/"

  logger.info("Starting updateCommentsPipeline")
  val updateCommentsPipeline = Source.fromPublisher(WykopDatabase.wykops.source())
    .via(objToGetRequestFlow(w => safeStringToUri(w.url)))
    .via(fetcherFlow[String, Wykop]())
    .via(contentFlow)
    .via(onlyCommentsFlow)
    .runWith(Sink.fromSubscriber(WykopDatabase.comments.sink()))

  logger.info("Starting updateVotesPipeline")
  val updateVotesPipeline = Source.fromPublisher(WykopDatabase.wykops.source())
    .via(voterUrlsFromWykopFlow)
    .via(votersFlow)
    .runWith(Sink.fromSubscriber(WykopDatabase.votes.sink()))

  logger.info("Starting glownaPipeline")
  val glownaPipeline = Source(1 to 20)
    .map(i => HttpRequest(HttpMethods.GET, indexPageUrl(i)))
    .via(contentFetcherFlow)
    .via(indexParserFlow(true))

  logger.info("Starting wykopaliskoPipeline")
  val wykopaliskoPipeline = Source(1 to 100)
    .map(i => HttpRequest(HttpMethods.GET, indexIncomingPageUrl(i)))
    .via(contentFetcherFlow)
    .via(indexParserFlow(false))

  logger.info("Publishing Wykops to Cassandra")
  Source.combine(glownaPipeline, wykopaliskoPipeline)(Merge(_))
    .toMat(Sink.fromSubscriber(WykopDatabase.wykops.sink()))(Keep.both)
    .run()

  logger.info("Running")

  logger.info("Awaiting termination")
  system.awaitTermination()

}
