package wykopml

import java.util.regex.Pattern

import com.datastax.spark.connector.cql._
import com.datastax.spark.connector.mapper.{ColumnMapForReading, ColumnMapForWriting, ColumnMapper}
import com.datastax.spark.connector.types.{IntType, TextType}
import com.datastax.spark.connector.writer.{RowWriter, RowWriterFactory}
import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.languagetool.tokenizers.Tokenizer
import org.languagetool.tokenizers.pl.PolishWordTokenizer
import wykopml.spark.{LoadCommentsFromCassandra, WithSpark}
import com.datastax.spark.connector._
import scala.collection.JavaConverters._

object GenerateNGrams extends App with StrictLogging {

  private def preCleanupContent(content: String): String = {
    Option(content).map(_.toLowerCase.replaceAll("\\.\\.\\.", "…").replaceAll("@([^\\W]+):?", "").trim).getOrElse("")
  }

  if (args.size != 1) {
    println("Provide value N as argument")
    sys.exit(1)
  }

  WithSpark(GenerateNGrams.run)

  private def nGram = args(0).toInt

  private implicit def NGramWriterFactory = new RowWriterFactory[(List[String], Int)] {
    override def rowWriter(
      table: TableDef,
      selectedColumns: IndexedSeq[ColumnRef]
    ): RowWriter[(List[String], Int)] = new RowWriter[(List[String], Int)] {
      override def columnNames: Seq[String] = (1 to nGram).map(i => s"word${i}") ++ Seq("n")

      override def readColumnValues(data: (List[String], Int), buffer: Array[Any]): Unit = {
        for (i <- 0 until nGram) buffer.update(i, data._1(i))
        buffer.update(nGram, data._2)
      }
    }
  }

  private implicit def NGramColumnMapper = new ColumnMapper[(List[String], Int)] {
    override def newTable(keyspaceName: String, tableName: String): TableDef = TableDef(
      keyspaceName,
      tableName,
      (1 to nGram).map(i => ColumnDef(s"word${i}", PartitionKeyColumn, TextType)),
      Seq[ColumnDef](),
      Seq[ColumnDef](ColumnDef("n", RegularColumn, IntType))
    )

    override def columnMapForWriting(struct: StructDef, selectedColumns: IndexedSeq[ColumnRef]): ColumnMapForWriting = ???

    override def columnMapForReading(struct: StructDef, selectedColumns: IndexedSeq[ColumnRef]): ColumnMapForReading = ???
  }

  private def run(sc: SparkContext): Unit = {

    def loadSentencesFromCassandra: RDD[List[String]] = {
      val comments = LoadCommentsFromCassandra(sc)
      val contents = comments.map(comment => preCleanupContent(comment.content))
      val sentences = contents.mapPartitions {
        content =>
          val tokenizer = new PolishWordTokenizer()
          content.map(tokenize(tokenizer, _))
      }
      sentences
    }

    val sentences = loadSentencesFromCassandra.setName("sentences")

    sentences.filter(_.size >= nGram).flatMap {
      sentence =>
        sentence.sliding(nGram, 1)
    }.groupBy(gram => gram).map {
      case (gram, items) => (gram, items.size)
    }.saveAsCassandraTable("wykop", s"sentences${nGram}gram")

  }

  private def tokenize(tokenizer: Tokenizer, content: String): List[String] = {
    tokenizer.tokenize(content).iterator().asScala.map(_.trim).filter(isAllowedToken).toList
  }

  private def isAllowedToken = {
    val IsWord = Pattern.compile("\\p{L}+")
    val AllowedTokens = Set(".")
    (token: String) =>
      token.size > 0 && token.size < 20 && (AllowedTokens.contains(token) || IsWord.asPredicate().test(token)) && !token.startsWith("http")
  }

}
