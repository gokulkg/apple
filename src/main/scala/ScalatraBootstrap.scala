import org.scalatra._
import javax.servlet.ServletContext
import com.myweb.apple.controllers._
import org.slf4j.LoggerFactory
import scala.slick.jdbc.JdbcBackend.Database

class ScalatraBootstrap extends LifeCycle {

  override def destroy(context: ServletContext) {
    super.destroy(context)
  }
  override def init(context: ServletContext) {
    context.mount(new NewsRSSController, "/news_rss/*")
    context.mount(new CategoriesController, "/categories/*")
    context.mount(new TrendsController, "/trends/*")
    context.initParameters("org.scalatra.cors.allowedOrigins") = "*"
  }
}
