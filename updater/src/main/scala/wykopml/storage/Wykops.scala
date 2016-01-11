package wykopml.storage

import akka.actor.ActorSystem
import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams._
import wykopml.bo.Wykop

abstract class Wykops extends WykopsTable with RootConnector {

  private def buildQuery(item: Wykop) = {
    update.where(_.id.eqs(item.id))
      .modify(_.title.setTo(item.title))
      .and(_.description.setTo(item.description))
      .and(_.author.setTo(item.author))
      .and(_.tags.setTo(item.tags))
      .and(_.url.setTo(item.url))
      .and(_.numberOfPoints.setTo(item.numberOfPoints))
      .and(_.numberOfComments.setTo(item.numberOfComments))
      .and(_.publishedAt.setTo(item.publishedAt))
      .and(_.isOnMain.setTo(item.isOnMain))
  }

  def save(item: Wykop) = {
    buildQuery(item).future()
  }

  def fetchAll() = {
    select.fetch()
  }

  implicit object WykopArticleSummaryRequestBuilder extends RequestBuilder[WykopsTable, Wykop] {
    override def request(ct: WykopsTable, t: Wykop)(implicit session: Session, keySpace: KeySpace) = {
      buildQuery(t)
    }
  }

  def sink()(implicit actorSystem: ActorSystem) = this.subscriber(100, 2)

  def source() = this.publisher()

}
