package wykopml.storage

import akka.actor.ActorSystem
import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams._
import wykopml.bo.Comment

abstract class Comments extends CommentsTable with RootConnector {

  private def buildQuery(item: Comment) = {
    update.where(_.wykopId.eqs(item.wykopId)).and(_.id.eqs(item.id))
      .modify(_.who.setTo(item.who))
      .and(_.content.setTo(item.content))
      .and(_.rawContent.setTo(item.rawContent))
      .and(_.plusPoints.setTo(item.plusPoints))
      .and(_.minusPoints.setTo(item.minusPoints))
      .and(_.when.setTo(item.when))
      .and(_.commentIndex.setTo(item.commentIndex))
      .and(_.commentParent.setTo(item.parentCommentId))
  }

  def save(item: Comment) = {
    buildQuery(item).future()
  }

  def fetchAll() = {
    select.fetch()
  }

  implicit object CommentRequestBuilder extends RequestBuilder[CommentsTable, Comment] {
    override def request(ct: CommentsTable, t: Comment)(implicit session: Session, keySpace: KeySpace) = {
      buildQuery(t)
    }
  }

  def sink()(implicit actorSystem: ActorSystem) = this.subscriber(10, 2)

  def source() = this.publisher()

}
