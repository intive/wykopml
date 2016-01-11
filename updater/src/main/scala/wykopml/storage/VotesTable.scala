package wykopml.storage

import com.websudos.phantom.dsl._
import wykopml.bo.Vote

class VotesTable extends CassandraTable[VotesTable, Vote] {

  object wykopId extends IntColumn(this) with PartitionKey[Int]

  object who extends StringColumn(this) with PrimaryKey[String]

  object isUp extends BooleanColumn(this)

  object when extends DateTimeColumn(this)

  override def fromRow(r: Row): Vote = {
    Vote(
      wykopId(r),
      who(r),
      isUp(r),
      when(r)
    )
  }
}
