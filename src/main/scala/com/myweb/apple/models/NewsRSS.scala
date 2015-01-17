package com.myweb.apple.models
import scala.slick.driver.MySQLDriver.simple._
import scala.collection.JavaConversions._
import us.monoid.json.JSONObject
import com.myweb.apple.models._
import com.myweb.apple.MysqlDB
import com.myweb.apple._
import scala.math._
import org.json4s.{ DefaultFormats, Formats }
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import org.slf4j.LoggerFactory
import scala.util.{ Try, Success, Failure }
import com.myweb.apple.models.CategoryTables._
import org.joda.time.DateTime
import com.myweb.apple.util.RssIndexer
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._
import com.myweb.apple.util.GooseHelper
import scala.concurrent.ExecutionContext.Implicits.global

case class NewsRSS(val id: Option[Int], val name: String, val rss: String, dateFormat: String) {
  import NewsRSS._

  def newsTuples(): Future[Seq[(String, DateTime, String)]] = {
    val rssIndexer = new RssIndexer(rss, dateFormat)
    rssIndexer.urlsWithDateAndTitle
  }

  def convertToNews(tuple: (String, DateTime, String)): Future[News] = {
    val categorySet: Set[String] = categories.map(_.name).toSet
    val content = GooseHelper.getCleanedTextAsFuture(tuple._1)
    content map (c => News(tuple._3, tuple._1, tuple._2.getMillis(), categorySet, c.fold("")(x => x)))
  }

  def getToNewsList = {
    newsTuples.map(_.map(convertToNews)).flatMap(Future.sequence(_))
  }

  def update: Boolean = {
    MysqlDB.db withDynSession {
      news_rss.insertOrUpdate(this) match {
        case 1 => true
        case _ =>
          logger.error("Some error hapended updating rss id:" + id)
          false
      }
    }
  }

  def delete: Boolean = {
    MysqlDB.db withDynSession {
      news_rss.filter(_.id === id).delete match {
        case 1 => true
        case _ =>
          logger.error("Some error hapended deleting NewsRss id:" + id)
          false
      }
    }
  }

  def categories: List[Category] = {
    MysqlDB.db withDynSession {
      (news_rss_categories.filter(_.newsRssId === id.get).flatMap(_.veritcalfk)).list
    }
  }

  def addCategories(categories: List[Category]): Boolean = {
    MysqlDB.db withDynSession {
      val res = Try(categories.map(v => news_rss_categories.insert(id.get, v.id.get)))
      res match {
        case Success(v) => true
        case Failure(e) =>
          logger.error("Some error happened adding categories" + e.getMessage())
          false
      }
    }
  }

  def deleteCategory(category: Category): Boolean = {
    MysqlDB.db withDynSession {
      news_rss_categories.filter(pv => pv.newsRssId === id.get && pv.categoryId === category.id.get).delete match {
        case 1 => true
        case _ => false
      }
    }
  }


  def toMap = {
    Map("id" -> id.get, "name" -> name, "rss" -> rss, "date_format" -> dateFormat, "categories" -> categories.map(_.toMap))
  }
}

class NewsRSSs(tag: Tag) extends Table[NewsRSS](tag, "news_rss") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def name = column[String]("NAME")
  def rss = column[String]("RSS")
  def date_format = column[String]("DATE_FORMAT")
  def * = (id.?, name, rss, date_format) <> ((NewsRSS.apply _).tupled, NewsRSS.unapply)

}

object NewsRSS {
  lazy val news_rss = TableQuery[NewsRSSs]
  val logger = LoggerFactory.getLogger(getClass)

  def insert(name: String, rss: String, dateFormat: String): Option[NewsRSS] = {
    try {
      MysqlDB.db withDynSession {
        news_rss returning news_rss.map(_.id) insert (NewsRSS(None, name, rss, dateFormat)) match {
          case id: Int => Some(NewsRSS(Some(id), name, rss, dateFormat))
        }
      }
    }
  }

  def all: List[NewsRSS] = {
    MysqlDB.db withDynSession {
      news_rss.list
    }
  }

  def fetchAndInsert: Future[List[Long]] = {
    Future.sequence(all.map(_.getToNewsList)).map(_.flatten).flatMap(l => Future.sequence(l.map(_.insertAsTrend)))
  }

  def get(id: Int): Option[NewsRSS] = MysqlDB.db withDynSession {
    news_rss.filter { _.id === id }.firstOption
  }

}