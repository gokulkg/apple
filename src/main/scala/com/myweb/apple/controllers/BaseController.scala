package com.myweb.apple.controllers
import org.scalatra._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.json._
abstract class BaseController extends ScalatraServlet with JacksonJsonSupport {

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
}