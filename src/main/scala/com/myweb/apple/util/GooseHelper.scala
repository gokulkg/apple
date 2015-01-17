package com.myweb.apple.util
import dispatch._, Defaults._
import com.gravity.goose._
import com.gravity.goose.Article
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import scala.util.{ Try, Success, Failure }

object GooseHelper {
  lazy val logger = LoggerFactory.getLogger(getClass())
  lazy val goose = {
    val configuration = new Configuration();
    configuration.setEnableImageFetching(false);
    new Goose(configuration);
  }

  def getWebContent(link: String): scala.concurrent.Future[Option[String]] = {
    try {
      val svc = url(link)
      Http(svc OK as.String).option
    } catch {
      case e: java.net.URISyntaxException =>
        logger.error("Some error happened fetching url :" + link, e)
        future { None }
      case e: java.lang.IllegalArgumentException =>
        logger.error("Some error happened fetching url :" + link, e)
        future { None }
    }
  }

  def getText(html: Option[String]): Option[String] = {
    html match {
      case Some(s) =>
        val article: Article = goose.extractContent("http://tookitki.com", s) // Dummy url not used 
        val content: String = article.cleanedArticleText
        Some(content)
      case None => None
    }
  }

  def getCleanedTextAsFuture(url: String): Future[Option[String]] = {
    val htmlContent: Future[Option[String]] = getWebContent(url)
    htmlContent map getText
  }

  def getContent(url: String): String = {
    val future = getCleanedTextAsFuture(url)
    Await.result(future, Duration("200 seconds")).fold("")(x => x)
  }

}
