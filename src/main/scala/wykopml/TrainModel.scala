package wykopml

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import wykopml.spark.{LoadVotesFromCassandra, WithSpark}

object TrainModel extends App with StrictLogging {

  private def createModel(rank: Int, numIterations: Int, ratings: RDD[Rating], calculateMse: Boolean = false): (MatrixFactorizationModel, Option[Double]) = {
    val model = ALS.train(ratings, rank, numIterations)
    if (calculateMse) {

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

      val meanSquareError = ratesAndPreds.map {
        case ((user, product), (r1, r2)) =>
          val err = (r1 - r2)
          err * err
      }.mean()

      (model, Some(meanSquareError))
    } else {
      (model, None)
    }
  }

  val paths = Paths(".model")

  WithSpark {
    sc =>

      val votesRDD = LoadVotesFromCassandra(sc).setName("votes").cache()

      val userMappingsRDD = votesRDD.map(_.who).distinct().zipWithIndex.map(p => (p._1, p._2.toInt))
      userMappingsRDD.saveAsObjectFile(paths.userMappingsPath)
      val userMappings = userMappingsRDD.collectAsMap()
      val ratings = votesRDD.map {
        v => Rating(userMappings(v.who), v.wykopId, if (v.isUp) 1 else -3)
      }.cache()

      var bestModelAndMse: Option[(Int, MatrixFactorizationModel, Option[Double])] = None

      for (rank <- 100 to 200 by 10) {
        val (model, mse) = createModel(30, 10, ratings, true)
        println(s"For rank ${rank} mse is ${mse}")
        if (bestModelAndMse.isEmpty || bestModelAndMse.get._3.getOrElse(Double.MaxValue) > mse.getOrElse(0.0)) {
          println("Will use new trained model")
          bestModelAndMse = Some((rank, model, mse))
        }
      }

      bestModelAndMse.foreach {
        case (rank, model, mse) =>
          println(s"Saving model with rank ${rank} and MSE ${mse} to ${paths.modelPath}")
          model.save(sc, paths.modelPath)
      }
  }

}
