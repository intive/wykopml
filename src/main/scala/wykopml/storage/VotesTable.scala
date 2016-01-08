package wykopml.storage

import com.websudos.phantom.dsl._
import wykopml.bo.Vote

class VotesTable extends CassandraTable[VotesTable, Vote] {

  object wykopId extends LongColumn(this) with PartitionKey[Long]

  object who extends StringColumn(this) with PrimaryKey[String]

  object when extends DateTimeColumn(this)

  object isUp extends BooleanColumn(this)

  override def fromRow(r: Row): Vote = {
    Vote(
      wykopId(r),
      who(r),
      when(r),
      isUp(r)
    )
  }
}
