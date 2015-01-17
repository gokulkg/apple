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

case class Category(id: Option[Int], name: String) {
  import Category._
  import CategoryTables._

  def toMap = {
    Map("id" -> id.get, "name" -> name)
  }

  def update: Boolean = {
    MysqlDB.db withDynSession {
      categories.insertOrUpdate(this) match {
        case 1 => true
        case _ => false
      }
    }
  }

  def delete: Boolean = {
    MysqlDB.db withDynSession {
      categories.filter(_.id === id).delete match {
        case 1 => true
        case _ =>
          logger.error("Some error hapended deleting Category id:" + id)
          false
      }
    }
  }
}

class Categories(tag: Tag) extends Table[Category](tag, "categories") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def name = column[String]("NAME")

  def * = (id.?, name) <> ((Category.apply _).tupled, Category.unapply)
}

object Category {
  import CategoryTables._
  val logger = LoggerFactory.getLogger(getClass)

  def insert(name: String): Option[Category] = {
    try {
      MysqlDB.db withDynSession {
        categories returning categories.map(_.id) insertOrUpdate (Category(None, name)) match {
          case Some(id) => Some(Category(Some(id), name))
          case None =>
            logger.error("Something went wrong inserting page into data base")
            None
        }
      }
    }
  }

  def all: List[Category] = {
    MysqlDB.db withDynSession {
      categories.list
    }
  }
  
  def get(id: Int): Option[Category] = MysqlDB.db withDynSession {
    categories.filter { _.id === id }.firstOption
  }
}

object CategoryTables {
  val categories = TableQuery[Categories]
  class NewsCategories(tag: Tag) extends Table[(Int, Int)](tag, "news_rss_categories") {
    def newsRssId = column[Int]("news_rss_id")
    def categoryId = column[Int]("category_id")
    def pk = primaryKey("pk_vews_rss_categories", (newsRssId, categoryId))

    def newsfk = foreignKey("NEWS_FK", newsRssId, TableQuery[NewsRSSs])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def veritcalfk = foreignKey("VERTICAL_FK", categoryId, categories)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def * = (newsRssId, categoryId)
  }
  val news_rss_categories = TableQuery[NewsCategories]
}