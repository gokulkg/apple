package com.myweb.apple.util

import spray.json._
import com.myweb.apple.models._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

object MywebJSONProtocol extends DefaultJsonProtocol {
  implicit object TrendJsonFormat extends RootJsonFormat[Trend] {
    def write(d: Trend) = JsObject("id" -> JsString(d.id), "url" -> JsString(d.url),
                                      "source" -> JsString(d.source), "source_time" -> JsString(DateTimeFormat.forPattern("yy-MM-dd").print(d.sourceTime)),
                                      "content" -> JsString(d.content))
    def read(v: JsValue) = ???
  }
  implicit object DocsByDate extends RootJsonFormat[Map[String, Seq[Trend]]] {
    def write(docsByDate: Map[String, Seq[Trend]]) = {
      val jsonbyDate = docsByDate.toVector.map(dateAndDocs => {
        val docsJson = dateAndDocs._2.map(_.toJson)
        JsObject("date" -> JsString(dateAndDocs._1), "docs" -> JsArray(docsJson.toVector))
      })
      JsArray(jsonbyDate)
    }
    def read(v: JsValue) = ???
  }
}
