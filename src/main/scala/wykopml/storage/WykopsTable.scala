package wykopml.storage

import java.time.LocalDateTime

import com.websudos.phantom.dsl._
import wykopml.bo.Wykop

class WykopsTable extends CassandraTable[WykopsTable, Wykop] {

  object id extends LongColumn(this) with PartitionKey[Long]

  object title extends StringColumn(this)

  object description extends StringColumn(this)

  object author extends StringColumn(this)

  object tags extends SetColumn[WykopsTable, Wykop, String](this)

  object url extends StringColumn(this)

  object numberOfPoints extends IntColumn(this)

  object numberOfComments extends IntColumn(this)

  object publishedAt extends DateTimeColumn(this)

  object isOnMain extends BooleanColumn(this)

  def fromRow(row: Row): Wykop = {

    Wykop(
      id(row),
      title(row),
      description(row),
      author(row),
      tags(row),
      url(row),
      numberOfPoints(row),
      numberOfComments(row),
      LocalDateTime.now(), //publishedAt(row), //FIXME: wrong date saved by phantom!? :-/
      isOnMain(row)
    )
  }
}
