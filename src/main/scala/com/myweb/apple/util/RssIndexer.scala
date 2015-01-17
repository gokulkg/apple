package com.myweb.apple.util

import dispatch._, Defaults._
import scala.concurrent.future
import java.net.URL
import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class RssIndexer(urlString: String, dateForamt: String) {
  def urlsWithDateAndTitle: Future[Seq[(String, DateTime, String)]] = {
    try {
      new java.net.URL(urlString)
      val optionalRespInFuture = Http(url(urlString) OK as.xml.Elem).option
      val optionalurlsWithDateAndTimeInFuture = for (optionalResp <- optionalRespInFuture)
        yield for (resp <- optionalResp)
        yield parseXMLOutput(resp)
      optionalurlsWithDateAndTimeInFuture.map(_.fold(Seq(): Seq[(String, DateTime, String)])(x => x))
    } catch {
      case e: java.net.MalformedURLException => future { Seq() }
    }

  }

  def parseXMLOutput(x: scala.xml.Elem): Seq[(String, DateTime, String)] = {
    val rss = x \\ "rss"
    (rss \\ "channel" \\ "item").map(i => {
      val link = (i \\ "link").text
      val time = DateTimeFormat.forPattern(dateForamt).parseDateTime((i \\ "pubDate").text)
      val title = (i \\ "title").text
      (link, time, title)
    })
  }
}
