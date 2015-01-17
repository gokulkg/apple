package com.myweb.apple.controllers

import com.myweb.apple._
import com.myweb.apple.models._
import com.myweb.apple.util._
import MywebJSONProtocol._

import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.json._
import org.scalatra._
import spray.json._
import DefaultJsonProtocol._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.concurrent._

class TrendsController extends BaseController with FutureSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  get("/query") {
    new AsyncResult {
      val is = docsFromQuery.map(_.toMap)
    }
  }

  private def docsFromQuery: Future[Map[String, Seq[Trend]]] = {
    val optionalStartDate = params.get("start_date").map(dateStrToDate)
    val optionalEndDate = params.get("end_date").map(dateStrToDate)
    val optionalSource = params.get("source")
    val optionalCategories = params.get("categories")
    (optionalStartDate, optionalEndDate, optionalSource, optionalCategories) match {
      case (Some(startDate), Some(endDate), None, None) => QueryByDateRange.trends(new DateTime(startDate), new DateTime(endDate))
      case (None, None, Some(source), None) => QueryBySource.trends(source)
      case (None, None, None, Some(categories)) => QueryByCategories.trends(categories)
      case (Some(startDate), Some(endDate), Some(source), None) => QueryByDateAndSource.trends(new DateTime(startDate), new DateTime(endDate), source)
      case (None, None, Some(source), Some(categories)) => QueryByCategoriesAndSource.trends(categories, source)
      case (Some(startDate), Some(endDate), None, Some(categories)) => QueryByDateAndCategories.trends(new DateTime(startDate), new DateTime(endDate), categories)
      case (Some(startDate), Some(endDate), Some(source), Some(categories)) => QueryByDateAndCategoriesAndSource.trends(new DateTime(startDate), new DateTime(endDate), categories, source)
      case _ => future{Map()}
    }
  }

  private def dateStrToDate(str: String): DateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(str)
}
