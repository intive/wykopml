package wykopml.parsers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.StrictLogging
import net.ruippeixotog.scalascraper.scraper.ContentExtractors._
import org.jsoup.nodes.Element
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import wykopml.bo.Wykop

import scala.util.matching.Regex

case object IndexParser extends StrictLogging {

  private val browser = Browser()

  def parseIndex(content: String, isMainPage: Boolean): List[Wykop] = {
    val doc = browser.parseString(content)

    doc >> elementList("ul#itemsStream li.iC") flatMap {
      e: Element =>
        val idString = e.select(".article.dC").attr("data-id")

        if (idString.length > 0) {
          val id = idString.toInt

          val titleLink = e.select(".m-reset-margin h2 a")
          val title = titleLink.attr("title")
          val url = titleLink.attr("href")

          val description = e.select("div.description p").text().trim

          val numberOfPoints = e.select(".diggbox a span").first().text().trim match {
            case "+"          => Int.MinValue
            case NormalInt(t) => t.toInt
            case p => {
              logger.warn(s"Unknown number of poits '${p}'")
              0
            }
          }

          if (numberOfPoints >= 0) {
            val publishedAt = LocalDateTime.parse(e.select("time").attr("datetime"), DateTimeFormatter.ISO_DATE_TIME)
            val tags = e >> elementList("a.tag") map {
              _.text()
            } filter {
              _.startsWith("#")
            } map {
              _.stripPrefix("#")
            } toSet

            val numberOfComments = e.select("i.fa-comments-o").first().parent().text().split(" ").headOption.flatMap(_.toOptInt()).getOrElse(0)

            val author = e.select(".fix-tagline a").first().text().trim.stripPrefix("@")

            Some(Wykop(id, title, description, author, tags, url, numberOfPoints, numberOfComments, publishedAt, isMainPage))
          } else {
            logger.debug("Skipped parsing of {}, looks like advert", e)
            None
          }
        } else {
          logger.debug("Skipped parsing of {}", e)
          None
        }
    }
  }

}
