package wykopml

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import wykopml.spark.WithSpark

object Prediction extends App with StrictLogging {

  if (args.size < 1) {
    println("Please provide path to model as an argument")
    sys.exit(1)
  }

  val paths = Paths(args(0))

  WithSpark {
    sc =>
      val model = MatrixFactorizationModel.load(sc, paths.modelPath)
      val userMappings = sc.objectFile[(String, Int)](paths.userMappingsPath).collectAsMap()

      val userToRecommend: String = if (args.size >= 2) args(1) else "lustefaniak"
      println(s"Will recommend for ${userToRecommend}")
      val userMappedId = userMappings(userToRecommend)

      model.recommendProducts(userMappedId, 10).foreach(r => println(s"${r.product} @ ${r.rating}"))

  }

}
