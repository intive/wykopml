package wykopml.storage

import akka.actor.ActorSystem
import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams._
import wykopml.bo.Vote

abstract class Votes extends VotesTable with RootConnector {

  private def buildQuery(item: Vote) = {
    update.where(_.wykopId.eqs(item.wykopId)).and(_.who.eqs(item.who))
      .modify(_.isUp.setTo(item.isUp))
      .and(_.when.setTo(item.when))
  }

  def save(item: Vote) = {
    buildQuery(item).future()
  }

  def fetchAll() = {
    select.fetch()
  }

  implicit object VoteRequestBuilder extends RequestBuilder[VotesTable, Vote] {
    override def request(ct: VotesTable, t: Vote)(implicit session: Session, keySpace: KeySpace) = {
      buildQuery(t)
    }
  }

  def sink()(implicit actorSystem: ActorSystem) = this.subscriber(10, 2)

  def source() = this.publisher()

}
