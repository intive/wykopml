package wykopml.spark

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.{SparkConf, SparkContext}

case object WithSpark extends StrictLogging {

  def apply(fn: (SparkContext) => Unit): Unit = {
    try {
      val sparkConfig = new SparkConf().setAppName("wykopml")
        .set("spark.executor.memory", "4g")
        .set("spark.cassandra.connection.host", "127.0.0.1")
        .setMaster("local[*]")
      val sc = new SparkContext(sparkConfig)
      try {
        fn(sc)
      } catch {
        case t: Throwable =>
          logger.error("Exception when executing with spark", t)
          throw t
      } finally {
        sc.stop()
      }
    } catch {
      case t: Throwable =>
        logger.error("WithSpark has failed", t)
        sys.exit(1)
    } finally {
      sys.exit(0)
    }
  }

}
