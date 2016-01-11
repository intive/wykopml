
package object wykopml {

  implicit class ReverseMap[K, V](underlying: scala.collection.Map[K, V]) {
    def getReverse(value: V) = underlying.find(_._2 == value).map(_._1)
  }

}
