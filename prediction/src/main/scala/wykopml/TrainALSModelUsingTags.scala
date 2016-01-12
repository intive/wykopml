package wykopml

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.mllib.recommendation.Rating
import wykopml.spark.{LoadVotesFromCassandra, LoadWykopsFromCassandra, WithSpark}
import scala.collection.immutable.Map

object TrainALSModelUsingTags extends App with StrictLogging {

  val paths = Paths(".model_tags")

  private def adjustTagPopularity(tags: Map[String, Long]): Map[String, Double] = {
    tags.filter(_._2 >= 20)
    val numberOfTags = tags.size
    val averageScore = tags.values.map(_.toDouble / numberOfTags).sum
    tags.map {
      case (tag, votes) => (tag, averageScore / votes)
    }
  }

  WithSpark {
    sc =>

      val numIterations = 18
      val rank = 90

      val votesRDD = LoadVotesFromCassandra(sc)
      val userMappingsRDD = votesRDD.map(_.who).distinct().zipWithIndex.map(p => (p._1, p._2.toInt))
      val userMappings = userMappingsRDD.collectAsMap()

      val tagsPerWykopRDD = LoadWykopsFromCassandra(sc).map { w =>
        (w.id, w.tags)
      }
      val tagPopularity = adjustTagPopularity(tagsPerWykopRDD.flatMap(_._2).countByValue().toMap)

      val tagRatings = votesRDD.map(v => (v.wykopId, v.who)).groupByKey().join(tagsPerWykopRDD).flatMap {
        case (wykopId, (people, tags)) =>
          for {
            tag <- tags
            who <- people
            tagPopularity <- tagPopularity.get(tag)
            score = 1 / tagPopularity
          } yield ((userMappings(who), wykopId), score)
      }.groupByKey().map {
        case ((who, wykopId), scores) =>
          Rating(who, wykopId, scores.sum / scores.size)
      }

      val (model, _) = TrainALS.createModel(rank, numIterations, tagRatings, shouldCalculateMse = false)

      println(s"Saving user mappings to ${paths.userMappingsPath}")
      userMappingsRDD.saveAsObjectFile(paths.userMappingsPath)
      println(s"Saving model with rank ${rank} to ${paths.modelPath}")
      model.save(sc, paths.modelPath)

  }

}
