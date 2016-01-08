package wykopml.bo

import java.time.LocalDateTime

case class Wykop(
    id: Long,
    title: String,
    description: String,
    author: String,
    tags: Set[String],
    url: String,
    numberOfPoints: Int,
    numberOfComments: Int,
    publishedAt: LocalDateTime,
    isOnMain: Boolean
) {
  def upVotersUrl = s"http://www.wykop.pl/ajax2/links/Upvoters/${id}/"

  def downVotersUrl = s"http://www.wykop.pl/ajax2/links/downvoters/${id}/"

}

case class Comment(
  wykopId: Long,
  id: Long,
  who: String,
  content: String,
  rawContent: String,
  plusPoints: Int,
  minusPoints: Int,
  when: LocalDateTime,
  commentIndex: Int,
  parentCommentId: Option[Long] = None
)

case class Vote(
  wykopId: Long,
  who: String,
  when: LocalDateTime,
  isUp: Boolean
)