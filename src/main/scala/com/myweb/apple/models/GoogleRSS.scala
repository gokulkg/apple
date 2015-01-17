package com.myweb.apple.models

import com.myweb.apple.models._
import com.myweb.apple._
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._
import us.monoid.json.JSONObject
import us.monoid.json.JSONArray
import scala.concurrent.ExecutionContext.Implicits.global
import com.myweb.apple.util._
import org.joda.time.DateTime
import scala.util.{ Try, Success, Failure }
import scala.tools.nsc.transform.Flatten

case class GoogleRSS(val title: String, val url: String, val createdAt: Long, val category: String) {
  def getContent: Future[Option[String]] = {
    GooseHelper.getCleanedTextAsFuture(url)
  }

  def convertToNews(content: Option[String]): News = {
    News(title, url, createdAt, Set(category), content.fold("")(x => x))
  }
}

object GoogleRSS {
  val baseUrl: String = "https://news.google.com/?output=rss"
  val dateFormat = """EEE, dd MMM yyyy HH':'mm':'ss 'GMT'"""  

  def getCountryCode(market: String): String = {
    market.split("-").last.toUpperCase()
  }

  def fetch(category: String): Future[List[News]] = {
    val url = baseUrl + s"""&q=${category}"""
    val rss = NewsRSS(None, "google_rss", url, dateFormat)
    rss.newsTuples.flatMap(tuples => Future.sequence(tuples.map(convertToNews(_)).toList))
  }
  
  def convertToNews(tuple: (String, DateTime, String)): Future[News] = {
    val categorySet: Set[String] =Set(); //categories.map(_.name).toSet
    val url = tuple._1.split("url=").last
    val content = GooseHelper.getCleanedTextAsFuture(url)
    content map (c => News(tuple._3, url, tuple._2.getMillis(), categorySet, c.fold("")(x => x)))
  }

  def insertNews(category: String): Future[List[Long]] = {
    fetch(category).flatMap(news => Future.sequence(news.map(_.insertAsTrend)))
  }

  def fetchAndInsert(): Future[List[Long]] = {
    val categories = Category.all.map(_.name)
    Future.sequence(categories.map(x => insertNews(x))).map(_.flatten)
  }
}