package com.myweb.apple.util

import scala.collection.JavaConversions._
import us.monoid.web.auth.RestyAuthenticator
import org.apache.commons.httpclient.util.URIUtil
import us.monoid.json.JSONObject
import dispatch._
import Defaults._
import dispatch.oauth._
import com.ning.http.client.oauth._
import com.ning.http.client.oauth.OAuthSignatureCalculator
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._
import com.ning.http.client.Response
import scala.util.parsing.json.JSON
import scala.util.{ Try, Success, Failure }

object HttpClient {
  import org.slf4j.LoggerFactory
  val logger = LoggerFactory.getLogger(getClass)
  def resty = new us.monoid.web.Resty
  def get(url: String): JSONObject = {
    synchronized {
      resty.json(url).toObject
    }
  }

  def getBody(res: Response): Option[JSONObject] = {
    if (res.getStatusCode() == 200) {
      Some(new JSONObject(res.getResponseBody()))
    } else {
      logger.error("Got status code :" + res.getStatusCode())
      None
    }
  }

  def getUsingOauth(key: String, secret: String, link: String): Future[Option[JSONObject]] = {
    try {
      val consumer = new ConsumerKey(key, secret)
      val token = new RequestToken("", "")
      val request = url(URIUtil.encodeQuery(link)).GET.sign(consumer, token) <:< Map("Accept" -> "application/json")
      val response = Http(request.GET)
      response map getBody
    } catch {
      case e: java.lang.IllegalArgumentException =>
        logger.error("Illegel url :" + link)
        future { None }
    }
  }

  def getAsFuture(link: String): Future[Option[JSONObject]] = {
    try {
      val request = url(link).GET <:< Map("Accept" -> "application/json")
      val response = Http(request.GET)
      response map getBody
    } catch {
      case e: java.lang.IllegalArgumentException =>
        logger.error("Illegel url :" + link)
        future { None }
    }
  }
}
