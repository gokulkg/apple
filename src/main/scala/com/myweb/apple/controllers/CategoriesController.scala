package com.myweb.apple.controllers
import com.myweb.apple._
import com.myweb.apple.models._
import com.myweb.apple.models.CategoryTables._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.json._
import org.scalatra._
import spray.json._
import DefaultJsonProtocol._
import org.json4s.JsonAST._
import com.myweb.apple.util.TimeHelper
import scala.util.{ Try, Success, Failure }
import scala.util.Failure
class CategoriesController extends ScalatraServlet with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats
  before() {
    contentType = formats("json")
  }

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
    Category.all.map(_.toMap)
  }

  post("/") {
    val json = parsedBody
    val categories: List[Category] = render(json \ "categories").extract[List[Category]]
    val res = Try(categories.map(v => Category.insert(v.name)))
    res match {
      case Success(s) => Ok("""{ "sucess": "Insserted Successfully"}""")
      case Failure(e) => ExpectationFailed(s"""{"error": "Something went wrong "}""")
    }
  }

  post("/category") {
    val json = parsedBody
    val category: Category = render(json \ "category").extract[Category]
    val res = Try(Category.insert(category.name))
    res match {
      case Success(s) => Ok("""{ "sucess": "Insserted Successfully"}""")
      case Failure(e) => ExpectationFailed(s"""{"error": "Something went wrong "}""")
    }
  }

  put("/:id") {
    val id = params.get("id").get.toInt
    val category: Option[Category] = Category.get(id)
    category.get.update match {
      case true => Ok("""{ "sucess": "Updated Successfully"}""")
      case _    => ExpectationFailed(s"""{"error": "Something went wrong "}""")
    }
  }

  delete("/:id") {
    val id = params.get("id").get.toInt
    Category.get(id) match {
      case Some(p) =>
        if (p.delete) Ok("""{ "sucess": "Deleted Successfully"}""")
        else ExpectationFailed(s"""{"error": "Some error happened deleting Category with id: ${id} "}""");
      case _ => ExpectationFailed(s"""{"error": "There is no Category with id: ${id} "}""")
    }
  }

}
