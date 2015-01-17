package com.myweb.apple.controllers
import com.myweb.apple._
import com.myweb.apple.models._
import com.myweb.apple.models.Category._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.json._
import org.scalatra._
import spray.json._
import DefaultJsonProtocol._
import org.json4s.JsonAST._
import com.myweb.apple.util.TimeHelper

class NewsRSSController extends BaseController {

  protected implicit val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.

  options("/*") {
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    response.setHeader("Access-Control-Allow-Methods", "DELETE,GET,PUT,POST,OPTIONS");
  }

  after() {
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    response.setHeader("Access-Control-Allow-Methods", "DELETE,GET,PUT,POST,OPTIONS");
  }

  get("/") {
    NewsRSS.all.map(_.toMap)
  }

  post("/") {
    val json = parsedBody
    val name = render(json \ "name") match { case JString(s) => s case _ => "" }
    val rss = render(json \ "rss") match { case JString(s) => s case _ => "" }
    val dateFormat = render(json \ "date_format") match { case JString(s) => s case _ => "" }
    val categories: List[Category] = render(json \ "categories").extract[List[Category]]

    if (name.isEmpty() || rss.isEmpty())
      BadRequest("One of the mandatory parameter is missing")
    else {
      NewsRSS.insert(name, rss, dateFormat) match {
        case Some(p) =>
         
          val res= p.addCategories(categories)
          if (res)
            Ok("""{"success" : "rss added Sucessfully"}""")
          else
            ExpectationFailed("""{"success": "rss added paritialy", "error": "Somthing went wrong adding categories"}""")
        case None => ExpectationFailed("""{"failed": "Somthing went wrong"}""")
      }
    }
  }

  put("/:id") {
    val id = params.get("id").get.toInt
    val json = parsedBody
    val name = render(json \ "name") match { case JString(s) => s case _ => "" }
    val rss = render(json \ "rss") match { case JString(s) => s case _ => "" }
    val dateFormat = render(json \ "date_format") match { case JString(s) => s case _ => "" }

    val newsRSS = NewsRSS(Some(id), name, rss, dateFormat)
    newsRSS.update match {
      case true  => Ok("""{ "sucess": "Updated Successfully"}""")
      case false => ExpectationFailed(s"""{"error": "Some error happened updating news_rss id: ${id} "}""");
    }
  }

  delete("/:id") {
    val id = params.get("id").get.toInt
    NewsRSS.get(id) match {
      case Some(p) =>
        if (p.delete) Ok("""{ "sucess": "Deleted Successfully"}""")
        else ExpectationFailed(s"""{"error": "Some error happened deleting news_rss id: ${id} "}""");
      case _ => ExpectationFailed(s"""{"error": "There is no NewsRSS with news_rss id: ${id} "}""")
    }
  }
 
  post("/:id/categories") {
    val json = parsedBody
    val rssId = params.get("id").get.toInt
    val categories: List[Category] = render(json \ "categories").extract[List[Category]]
    val newsRss = NewsRSS.get(rssId)
    if (categories.size == 0)
      BadRequest("No categories specified in the request")
    else if (newsRss.equals(None))
      BadRequest("news_rss id  specified in the request not in the data base")
    else {
      newsRss.get.addCategories(categories) match {
        case true => Ok("""{"success" : "Category added"}""")
        case _    => ExpectationFailed("""{"error" : "Ooops Somthing went wrong"}""")
      }
    }
  }

  delete("/:id/categories/:category_id") {
    val rssId = params.get("id").get.toInt
    val categoryId = params.get("category_id").get.toInt
    val rss = NewsRSS.get(rssId)
    val category = Category.get(categoryId)
    if (rss.equals(None))
      BadRequest("news_rss id  specified in the request not in the data base")
    else if (category.equals(None))
      BadRequest("category id  specified in the request not in the data base")
    else {
      rss.get.deleteCategory(category.get) match {
        case true => Ok("""{ "sucess": "Deleted Successfully"}""")
        case _    => ExpectationFailed("""{"error": "Somthing went wrong"}""")
      }
    }
  }

  }
