package wykopml

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import wykopml.spark.WithSpark

object Prediction extends App with StrictLogging {

  val paths = Paths(".model")

  WithSpark {
    sc =>
      val model = MatrixFactorizationModel.load(sc, paths.modelPath)
      val userMappings = sc.objectFile[(String, Int)](paths.userMappingsPath).collectAsMap()

      println("=====================")
      println("\n\n\n\n\n")

      val userToRecommend = "lustefaniak"
      println(s"Will recommend for ${userToRecommend}")
      val userMappedId = userMappings("lustefaniak")

      println(model.predict(userMappedId, 2944301))

      model.recommendProducts(userMappedId, 10).map {
        rating =>
          println(rating)
          println(userMappings.getReverse(rating.user))
          println(rating.product)

      }

      println("\n\n\n\n\n")
      println("=====================")
      println("\n\n\n\n\n")
  }

}
