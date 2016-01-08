package wykopml

import java.time.{ZoneOffset, LocalDateTime}

import org.joda.time.DateTime
import scala.language.implicitConversions

package object storage {

  implicit def jodaDtToLocalDt(dt: DateTime): LocalDateTime = LocalDateTime.ofEpochSecond(dt.toInstant.getMillis, 0, ZoneOffset.UTC)

  implicit def localDtToJodaDt(ldt: LocalDateTime): DateTime = DateTime.parse(ldt.toString)

}
