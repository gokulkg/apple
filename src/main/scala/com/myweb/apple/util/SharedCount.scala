package com.myweb.apple.util
import us.monoid.json.JSONObject
import scala.util.{ Try, Success, Failure }
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
object SharedCount {
  val baseUrl = "http://free.sharedcount.com/?apikey=bcdde55df0411dd2a3f19a022d9f0842f7237cd2"
  def getScore(jsonObj: Option[JSONObject]): Float = {
    jsonObj match {
      case None =>
        0.toFloat
      case Some(json) => {
        if (json.has("Facebook")) {
          val likes = Try(json.getJSONObject("Facebook").getInt("like_count")) match { case Success(v) => v case Failure(e) => 0 }
          val shares = Try(json.getJSONObject("Facebook").getInt("share_count")) match { case Success(v) => v case Failure(e) => 0 }
          val comments = Try(json.getJSONObject("Facebook").getInt("comment_count")) match { case Success(v) => v case Failure(e) => 0 }
          (likes + (comments * 5) + (shares * 10)).toFloat
        } else
          0.toFloat
      }
    }
  }

  def score(link: String): Future[Float] = {
    val url = baseUrl + "&url=" + link
    HttpClient.getAsFuture(url) map getScore
  }

}