package com.myweb.apple
import javax.servlet.ServletContext
import com.myweb.apple.controllers._
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import scala.slick.jdbc.JdbcBackend.Database
import com.myweb.apple._
import com.myweb.apple.models.NewsRSS._
import com.myweb.apple.models.CategoryTables._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.collection.JavaConversions._

object MysqlDB {
  val logger = LoggerFactory.getLogger(getClass)
  val cpds = new ComboPooledDataSource
  lazy val db = {
    cpds.setDriverClass("com.mysql.jdbc.Driver")
    cpds.setJdbcUrl("jdbc:mysql://" + AppConfig.conf.getString(s"mysql.host") + ":3306/myweb")
    cpds.setUser(AppConfig.conf.getString(s"mysql.user"))
    cpds.setPassword(AppConfig.conf.getString(s"mysql.password"))
    cpds.setMinPoolSize(20)
    cpds.setAcquireIncrement(5)
    cpds.setMaxPoolSize(100)
    lazy val db = Database.forDataSource(cpds)
    logger.info("Created c3p0 connection pool")
    logger.info("Creating tables if not exist")
    createTables(db)
    db
  }

  def createTables(db: Database) {
    db withSession {
      implicit session =>
        createIfNotExists( categories, news_rss, news_rss_categories)
    }
  }

  def createIfNotExists(tables: TableQuery[_ <: Table[_]]*)(implicit session: Session) {
    logger.info("creating tables")
    tables foreach { table => if (MTable.getTables(table.baseTableRow.tableName).list.isEmpty) table.ddl.create }
  }

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    cpds.close
  }
}