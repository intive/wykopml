package wykopml

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import wykopml.TrainALS.EstimatedBestRank
import wykopml.spark.{LoadVotesFromCassandra, WithSpark}

object TrainALSModelUsingVotes extends App with StrictLogging {

  val paths = Paths(".model_votes")

  WithSpark {
    sc =>

      val numIterations = 15
      val rank = 90 //estimated using TrainALS.estimateBestRankValue (For rank 90 and 10 iterations mse is Some(0.06912949551198742))

      val votesRDD = LoadVotesFromCassandra(sc).setName("votes").cache()
      val userMappingsRDD = votesRDD.map(_.who).distinct().zipWithIndex.map(p => (p._1, p._2.toInt))
      val userMappings = userMappingsRDD.collectAsMap()
      val ratings = votesRDD.map {
        v => Rating(userMappings(v.who), v.wykopId, if (v.isUp) 1 else -3)
      }.cache()

      val (model, mse) = TrainALS.createModel(rank, numIterations, ratings, shouldCalculateMse = true)

      println(s"Saving user mappings to ${paths.userMappingsPath}")
      userMappingsRDD.saveAsObjectFile(paths.userMappingsPath)
      println(s"Saving model with rank ${rank} and MSE ${mse} to ${paths.modelPath}")
      model.save(sc, paths.modelPath)

  }

}
