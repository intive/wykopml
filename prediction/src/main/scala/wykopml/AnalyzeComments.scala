package wykopml

import java.util.regex.Pattern

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, SVMWithSGD}
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.regression.LabeledPoint
import org.languagetool.tokenizers.Tokenizer
import org.languagetool.tokenizers.pl.PolishWordTokenizer
import wykopml.spark.{LoadCommentsFromCassandra, WithSpark}

import scala.collection.JavaConverters._

object AnalyzeComments extends App with StrictLogging {

  private def preCleanupContent(content: String): String = {
    Option(content).map(_.toLowerCase.replaceAll("\\.\\.\\.", "…").replaceAll("@([^\\W]+):?", "").trim).getOrElse("")
  }

  WithSpark(AnalyzeComments.run)

  private def run(sc: SparkContext): Unit = {

    val hashingTF = new HashingTF(1000)

    val sentences = loadSentencesFromCassandra(sc).setName("sentences")

    val withFeatures = sentences.filter(_._5.size > 5).map {
      case (who, plus, minus, comment, tokens) =>
        (who, plus, minus, comment, tokens, hashingTF.transform(tokens))
    }

    val labeledPoints = withFeatures.map {
      case (who, plus, minus, comment, tokens, features) =>
        LabeledPoint(classifyCommentPoints(plus, minus), features)
    }

    val troll = labeledPoints.filter(_.label == 1.0)
    val ok = labeledPoints.filter(_.label < 1).sample(true, 0.3)

    val numIterations = 100
    val model = SVMWithSGD.train(troll ++ ok, numIterations)

    val valuesAndPreds = labeledPoints.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val MSE = valuesAndPreds.map { case (v, p) => math.pow((v - p), 2) }.mean()

    println("training Mean Squared Error = " + MSE)

  }

  private def classifyCommentPoints(plusPoints: Int, minusPoints: Int): Double = {
    val percentPossitive = plusPoints.toDouble / (plusPoints + minusPoints)
    if (percentPossitive < 0.33) 1 else 0
  }

  private def loadSentencesFromCassandra(sc: SparkContext) = {
    val comments = LoadCommentsFromCassandra(sc)
    val contents = comments.map(comment => (comment.who, comment.plusPoints, comment.minusPoints, preCleanupContent(comment.content)))
    val sentences = contents.mapPartitions {
      content =>
        val tokenizer = new PolishWordTokenizer()
        content.map {
          case (who, plus, minus, comment) =>
            (who, plus, minus, comment, tokenize(tokenizer, comment))
        }
    }
    sentences
  }

  private def tokenize(tokenizer: Tokenizer, content: String): List[String] = {
    tokenizer.tokenize(content).iterator().asScala.map(_.trim).filter(isAllowedToken).toList
  }

  private def isAllowedToken = {
    val IsWord = Pattern.compile("\\p{L}+").asPredicate()
    (token: String) =>
      token.size > 0 && token.size < 20 && IsWord.test(token) && !token.startsWith("http")
  }

}
