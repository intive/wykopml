package wykopml.spark

import java.time.{ZoneId, LocalDateTime}

import com.datastax.driver.core.Row
import com.datastax.spark.connector.{ColumnName, ColumnRef}
import com.datastax.spark.connector.cql.TableDef
import com.datastax.spark.connector.rdd.reader.{RowReader, RowReaderFactory}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import wykopml.bo.Vote
import com.datastax.spark.connector._

case object LoadVotesFromCassandra {

  def apply(sc: SparkContext): RDD[Vote] = {
    implicit val rowReaderFactory = new RowReaderFactory[Vote] {
      override def rowReader(table: TableDef, selectedColumns: IndexedSeq[ColumnRef]): RowReader[Vote] = new RowReader[Vote] {
        override def neededColumns: Option[Seq[ColumnRef]] = Some(
          Seq("wykopid", "who", "isup", "when").map(ColumnName(_))
        )

        override def read(row: Row, columnNames: Array[String]): Vote = {
          Vote(
            row.getInt("wykopid"),
            row.getString("who"),
            row.getBool("isup"),
            LocalDateTime.ofInstant(row.getTimestamp("when").toInstant, ZoneId.systemDefault())
          )

        }
      }

      override def targetClass: Class[Vote] = classOf[Vote]
    }

    sc.cassandraTable[Vote]("wykop", "votes")

  }

}
