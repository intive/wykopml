package wykopml

import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD

case object TrainALS {

  def createModel(rank: Int, numIterations: Int, ratings: RDD[Rating], calculateMse: Boolean = false): (MatrixFactorizationModel, Option[Double]) = {
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

  case class EstimatedBestRank(rank: Int, model: MatrixFactorizationModel, mse: Double)

  case class RankEstimationRanges(start: Int, to: Int, by: Int)

  val DefaultRankEstimationRanges = RankEstimationRanges(10, 100, 10)

  def estimateBestRankValue(
    ratings: RDD[Rating],
    numIterations: Int = 18,
    params: RankEstimationRanges = DefaultRankEstimationRanges,
    terminateIfNotBetter: Boolean = false
  ): Option[EstimatedBestRank] = {

    var bestModelAndMse: Option[EstimatedBestRank] = None

    for (rank <- params.start to params.to by params.by) {
      println(s"Creating model for rank ${rank} with ${numIterations} iterations.")
      val startTime = System.currentTimeMillis()
      val (model, Some(mse)) = createModel(rank, numIterations, ratings, true)
      val timeSpent = System.currentTimeMillis() - startTime
      println(s"Model creation  for rank ${rank} with ${numIterations} iterations took ${(timeSpent / 1000).toInt}s (${timeSpent}ms)")
      println(s"For rank ${rank} MSE is ${mse}.")
      val previousDesc = bestModelAndMse.map(b => s"${b.mse} @ ${b.rank}")
      if (bestModelAndMse.isEmpty || bestModelAndMse.get.mse > mse) {
        println(s"Will use trained model, previous best was ${previousDesc}.")
        bestModelAndMse = Some(EstimatedBestRank(rank, model, mse))
      } else {
        println(s"Previous best was $previousDesc, keeping it.")
        if (terminateIfNotBetter) {
          return bestModelAndMse
        }
      }
    }

    bestModelAndMse

  }

}
