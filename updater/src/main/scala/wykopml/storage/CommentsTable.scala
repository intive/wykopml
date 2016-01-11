package wykopml.storage

import com.websudos.phantom.dsl._
import wykopml.bo.Comment

class CommentsTable extends CassandraTable[CommentsTable, Comment] {

  object wykopId extends IntColumn(this) with PartitionKey[Int]

  object id extends LongColumn(this) with PrimaryKey[Long]

  object who extends StringColumn(this)

  object content extends StringColumn(this)

  object rawContent extends StringColumn(this)

  object plusPoints extends IntColumn(this)

  object minusPoints extends IntColumn(this)

  object when extends DateTimeColumn(this)

  object commentIndex extends IntColumn(this)

  object commentParent extends OptionalLongColumn(this)

  override def fromRow(r: Row): Comment = {
    Comment(
      wykopId(r),
      id(r),
      who(r),
      content(r),
      rawContent(r),
      plusPoints(r),
      minusPoints(r),
      when(r),
      commentIndex(r),
      commentParent(r)
    )
  }
}
