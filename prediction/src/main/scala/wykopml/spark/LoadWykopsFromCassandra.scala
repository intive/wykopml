package wykopml.spark

import java.time.{LocalDateTime, ZoneId}

import com.datastax.driver.core.Row
import com.datastax.spark.connector.cql.TableDef
import com.datastax.spark.connector.rdd.reader.{RowReader, RowReaderFactory}
import com.datastax.spark.connector.{ColumnRef, _}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import wykopml.bo.Wykop

import scala.collection.JavaConversions._

case object LoadWykopsFromCassandra {

  def apply(sc: SparkContext): RDD[Wykop] = {
    implicit val rowReaderFactory = new RowReaderFactory[Wykop] {
      override def rowReader(table: TableDef, selectedColumns: IndexedSeq[ColumnRef]): RowReader[Wykop] = new RowReader[Wykop] {
        override def neededColumns: Option[Seq[ColumnRef]] = None

        override def read(row: Row, columnNames: Array[String]): Wykop = {
          Wykop(
            row.getInt("id"),
            row.getString("title"),
            row.getString("description"),
            row.getString("author"),
            row.getSet[String]("tags", classOf[String]).toSet.filter(_.size > 0),
            row.getString("url"),
            row.getInt("numberofpoints"),
            row.getInt("numberofcomments"),
            LocalDateTime.ofInstant(row.getTimestamp("publishedat").toInstant, ZoneId.systemDefault()),
            row.getBool("isonmain")
          )

        }
      }

      override def targetClass: Class[Wykop] = classOf[Wykop]
    }

    sc.cassandraTable[Wykop]("wykop", "wykops")

  }

}
