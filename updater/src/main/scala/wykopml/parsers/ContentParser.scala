package wykopml.parsers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.LazyLogging
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.jsoup.nodes.Element
import wykopml.bo.{Comment, Wykop}

import scala.util.Try

case object ContentParser extends LazyLogging {

  type ContentResul = List[Comment]

  private val browser = Browser()

  def parse(content: String, wykop: Wykop): ContentResul = {
    val doc = browser.parseString(content)

    def parseComment(d: Element, commentIndex: Int, parent: Option[Long]): Comment = {
      Comment(
        wykop.id,
        d.attr("data-id").toLong,
        d.select(".showProfileSummary").first().text().trim,
        d.select("div.text").first().text().trim,
        d.select("div.text").first().html(),
        d.select("p.vC").attr("data-vcp").stripPrefix("+").toInt,
        d.select("p.vC").attr("data-vcm").toInt,
        LocalDateTime.parse(d.select("time").attr("datetime"), DateTimeFormatter.ISO_DATE_TIME),
        commentIndex,
        parent
      )
    }

    val comments = (doc >> elementList("ul#itemsStream.comments-stream li.iC")).zipWithIndex.flatMap {
      case (e: Element, commentIndex: Int) =>

        val comment = parseComment(e.select("div").first(), commentIndex, None)

        val subComments = (e >> elementList("ul.sub li")).zipWithIndex.flatMap {
          case (e: Element, commentIndex: Int) =>
            Option(e.select("div").first()).map {
              d: Element =>
                parseComment(d, commentIndex, Some(comment.id))
            }
        }

        comment +: subComments

    }

    comments

  }

}
