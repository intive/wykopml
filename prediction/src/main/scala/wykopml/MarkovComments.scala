package wykopml

import com.datastax.spark.connector._
import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.SparkContext
import wykopml.spark.WithSpark

import scala.annotation.tailrec
import scala.util.Random

object MarkovComments extends App with StrictLogging {

  case class UniGram(word1: String, n: Int)

  case class BiGram(word1: String, word2: String, n: Int)

  def startWords = if (args.size > 0) args.toSeq else Seq("korwin")

  WithSpark(MarkovComments.run)

  def nextRandomTransition[T](out: List[(T, Double)]): Option[T] = {
    val rnd = Random.nextDouble()

    @tailrec
    def returnIfProbabilityHigher(acc: Double, out2: List[(T, Double)]): Option[T] = {
      out2.headOption match {
        case Some((i, p)) =>
          if (acc <= rnd && rnd <= acc + p)
            Some(i)
          else returnIfProbabilityHigher(acc + p, out2.tail)
        case None => None
      }
    }
    returnIfProbabilityHigher(0.0, out)
  }

  private def run(sc: SparkContext): Unit = {

    val unigrams = sc.cassandraTable[UniGram]("wykop", "sentences1gram")

    val totalPopularity = unigrams.map(_.n).sum()

    val reverseWordPopularity = unigrams.map {
      case UniGram(word, n) =>
        val adjusted = 1 - n / totalPopularity
        (word, adjusted * adjusted * adjusted)
    }.collectAsMap()

    val bigrams = sc.cassandraTable[BiGram]("wykop", "sentences2gram")

    val transitions = bigrams.groupBy(_.word1).map {
      case (word1, transitions) =>
        val totalTransitions = transitions.map(_.n).sum.toDouble
        val out = transitions.map {
          case BiGram(_, word2, n) =>
            (word2, reverseWordPopularity(word2) * (n / totalTransitions))
        }
        val factor = 1.0 / out.map(_._2).sum
        val out2 = out.map {
          case (word, popularity) =>
            (word, popularity * factor)
        }
        (word1, out.toList)
    }.collectAsMap()

    @tailrec
    def createChain(currentWord: String, acc: List[String] = List()): List[String] = {
      transitions.get(currentWord).flatMap(nextRandomTransition) match {
        case None          => (currentWord :: acc).reverse
        case Some(newWord) => createChain(newWord, currentWord :: acc)
      }
    }

    for (word <- startWords) {
      for (i <- 1 to 100) {
        println(createChain("korwin").mkString(" "))
      }
    }

  }

}
