package wykopml

import scala.util.Try

package object parsers {

  implicit class RichOptionConvert(val s: String) extends AnyVal {
    def toOptInt() = Try(s.toInt) toOption
  }

  val NormalInt = """(\d*)""".r
}
