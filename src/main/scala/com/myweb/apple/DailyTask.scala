package com.myweb.apple
import com.myweb.apple.models._
import org.slf4j.LoggerFactory
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.sql.Timestamp
object DailyTask {
  val logger = LoggerFactory.getLogger(getClass)


  def fetchGoogleRSS = {
    logger.info("Starting Google News Rss fetching")
    Await.result(GoogleRSS.fetchAndInsert, Duration("1 day"))
    logger.info("Google rss fetching is completed")
    System.exit(1)
  }

  def main(args: Array[String]) {
    fetchGoogleRSS
  }
}