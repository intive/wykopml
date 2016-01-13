package wykopml.spark

import java.time.{LocalDateTime, ZoneId}

import com.datastax.driver.core.Row
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.TableDef
import com.datastax.spark.connector.rdd.reader.{RowReader, RowReaderFactory}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import wykopml.bo.Comment

case object LoadCommentsFromCassandra {

  def apply(sc: SparkContext): RDD[Comment] = {
    implicit val rowReaderFactory = new RowReaderFactory[Comment] {
      override def rowReader(table: TableDef, selectedColumns: IndexedSeq[ColumnRef]): RowReader[Comment] = new RowReader[Comment] {
        override def neededColumns: Option[Seq[ColumnRef]] = None

        override def read(row: Row, columnNames: Array[String]): Comment = {
          Comment(
            row.getInt("wykopid"),
            row.getLong("id"),
            row.getString("who"),
            row.getString("content"),
            row.getString("rawcontent"),
            row.getInt("pluspoints"),
            row.getInt("minuspoints"),
            LocalDateTime.ofInstant(row.getTimestamp("when").toInstant, ZoneId.systemDefault()),
            row.getInt("commentindex"),
            Option(row.getLong("commentparent"))
          )

        }
      }

      override def targetClass: Class[Comment] = classOf[Comment]
    }

    sc.cassandraTable[Comment]("wykop", "comments")

  }

}
