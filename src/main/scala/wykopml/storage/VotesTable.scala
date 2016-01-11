package wykopml.storage

import com.websudos.phantom.dsl._
import wykopml.bo.Vote

class VotesTable extends CassandraTable[VotesTable, Vote] {

  object wykopId extends LongColumn(this) with PartitionKey[Long]

  object who extends StringColumn(this) with PrimaryKey[String]

  object isUp extends BooleanColumn(this)

  object when extends DateTimeColumn(this)

  override def fromRow(r: Row): Vote = {
    Vote(
      wykopId(r).toInt,
      who(r),
      isUp(r),
      when(r)
    )
  }
}
