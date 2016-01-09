package wykopml

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import wykopml.storage.WykopDatabase

import scala.concurrent.Await
import scala.concurrent.duration._

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating

import scala.util.Try

object Prediction extends App with StrictLogging {

  val ModelPath = "/tmp/wykopml"

  implicit class ReverseMap[K, V](underlying: Map[K, V]) {
    def getReverse(value: V) = underlying.find(_._2 == value).map(_._1)
  }

  private def createModel(ratings: RDD[Rating]) = {

    def trainModel(ratings: RDD[Rating]) = {
      val numRatings = ratings.count
      val numUsers = ratings.map(_.user).distinct.count
      val numVotes = ratings.map(_.product).distinct.count

      logger.info("Got " + numRatings + " ratings from "
        + numUsers + " users on " + numVotes + " movies.")

      val rank = 20
      val numIterations = 18
      val model = ALS.train(ratings, rank, numIterations, 0.01)
      model
    }

    val model = trainModel(ratings)

    val userVotes = ratings.map {
      case Rating(who, wykop, rate) =>
        (who, wykop)
    }

    val predictions = model.predict(userVotes).map {
      case Rating(who, wykop, rate) =>
        ((who, wykop), rate)
    }

    val ratesAndPreds = ratings.map {
      case Rating(who, wykop, rate) =>
        ((who, wykop), rate)
    }.join(predictions)

    val MSE = ratesAndPreds.map {
      case ((user, product), (r1, r2)) =>
        val err = (r1 - r2)
        err * err
    }.mean()

    logger.info("Mean Squared Error = " + MSE)

    model
  }

  val sparkConfig = new SparkConf().setAppName("wykopml")
    .set("spark.executor.memory", "4g")
    .setMaster("local[*]")
  val sc = new SparkContext(sparkConfig)
  try {
    //FIXME: load directly from spark when cassandra-connector for 1.6.0 is released
    val votesRDD = sc.parallelize(Await.result(WykopDatabase.votes.fetchAll(), Duration.Inf))

    val userMappings = votesRDD.map(_.who).distinct().collect().zipWithIndex.toMap
    val wykopMappings = votesRDD.map(_.wykopId).distinct().collect().zipWithIndex.toMap

    val ratings = votesRDD.map {
      v => Rating(userMappings(v.who), wykopMappings(v.wykopId), if (v.isUp) 1 else -1)
    }

    val model: MatrixFactorizationModel = createModel(ratings)

    println("\n\n\n\n\n")
    println("=====================")
    println("\n\n\n\n\n")

    val userToRecommend = "lustefaniak"
    println(s"Will recommend for ${userToRecommend}")
    val userMappedId = userMappings("lustefaniak")

    println(model.predict(userMappedId, wykopMappings(2944301)))

    model.recommendProducts(userMappedId, 10).map {
      rating =>
        println(rating)
        println(userMappings.getReverse(rating.user))
        println(wykopMappings.getReverse(rating.product))

    }

    println("\n\n\n\n\n")
    println("=====================")
    println("\n\n\n\n\n")

  } catch {
    case e: Throwable =>
      logger.error("Some error", e)
      sys.exit(1)
  } finally {
    sc.stop()
    sys.exit(0)
  }

}
