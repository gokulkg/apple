package com.myweb.apple.models
import com.myweb.apple.models._
import com.myweb.apple._
import com.myweb.apple.util.HttpClient
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._
import us.monoid.json.JSONObject
import us.monoid.json.JSONArray
import scala.concurrent.ExecutionContext.Implicits.global
import com.myweb.apple.util.GooseHelper
import com.myweb.apple.util._
import org.joda.time.DateTime
case class News(val title: String, val url: String, val createdAt: Long, val categories: Set[String], val content: String) {

  def getScore: Future[Float] = {
    SharedCount.score(url)
  }

  def insertAsTrend: Future[Long] = {
    val doc = getScore map (s => Trend(categories,
      SHA256.get(content), url,
      "web", new DateTime(createdAt), s, Some(title), content))
    doc.flatMap(d => d.addToTrendRecordAndQueryTables)
  }
}

