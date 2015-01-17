package com.myweb.apple.models

import java.util.{ Date, UUID }
import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._
import com.websudos.phantom.zookeeper._
import com.myweb.apple._
import scala.concurrent.Await
import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.JavaConversions._

object TrendCassandraManager extends DefaultCassandraManager{
  override val cassandraHost: String = AppConfig.conf.getString("trends_cassandra.host")

  val keySpace = AppConfig.conf.getString("trends_cassandra.keyspace")
  val init: Unit = initIfNotInited(keySpace)
}

trait TrendCassandraConnector extends SimpleCassandraConnector {
  val keySpace = AppConfig.conf.getString("trends_cassandra.keyspace")
  override val manager: CassandraManager = TrendCassandraManager
}

case class Trend(categories: Set[String],
                    id: String, url: String,
                    source: String, sourceTime: DateTime, score: Float, title: Option[String], content: String){
  
  def addToTrendRecordAndQueryTables:Future[Long] = {
    val addIntoDocStore = TrendRecord.insertNewTrend(this)
    val addsInFuture:List[Future[Long]] = List(QueryByDateRange, QueryBySource, QueryByCategories,
                          QueryByDateAndSource, QueryByCategoriesAndSource, QueryByDateAndCategories,
                                          QueryByDateAndCategoriesAndSource).map(_.add(this))
    Future.sequence(addsInFuture :+ addIntoDocStore).map(_.reduceLeft(_ + _))
  }
}

sealed class TrendRecord extends CassandraTable[TrendRecord, Trend] {

  object categories extends SetColumn[TrendRecord, Trend, String](this)
  object id extends StringColumn(this) with PartitionKey[String]
  object url extends StringColumn(this)
  object source extends StringColumn(this)
  object content extends StringColumn(this)
  object sourceTime extends DateTimeColumn(this)
  object score extends FloatColumn(this)
  object title extends OptionalStringColumn(this)
  override def fromRow(row: Row): Trend = Trend(categories(row), id(row), url(row), source(row), sourceTime(row), score(row), title(row), content(row))
}

object TrendRecord extends TrendRecord with TrendCassandraConnector{
  lazy val init = Await.result(create.future, Duration(1000, MILLISECONDS))

  def insertNewTrend(queriedTrend: Trend): Future[Long] = {
    val resultSetInFuture = insert.value(_.categories, queriedTrend.categories)
                                  .value(_.id, queriedTrend.id)
                                  .value(_.url, queriedTrend.url)
                                  .value(_.source, queriedTrend.source)
                                  .value(_.score, queriedTrend.score)
                                  .value(_.title, queriedTrend.title)
                                  .value(_.content, queriedTrend.content)
                                  .value(_.sourceTime, queriedTrend.sourceTime).future
    resultSetInFuture.map(_.all.length)
  }

  def getTrendById(id: String): Future[Option[Trend]] = select.where(_.id eqs id).one

  def Trends(ids: Seq[String]): Future[Seq[Trend]] = select.where(_.id in ids.toList.take(500)).fetch

}
