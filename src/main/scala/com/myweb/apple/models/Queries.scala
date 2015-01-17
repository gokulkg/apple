package com.myweb.apple.models

import com.redis._
import serialization._
import Parse.Implicits._
import com.myweb.apple._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

abstract class QueryBy{
  lazy val redisConnectionPool  = new RedisClientPool(AppConfig.conf.getString("queries_redis.host"), AppConfig.conf.getInt("queries_redis.port"))

  def groupByDate(docs: Seq[Trend]): Map[String, Seq[Trend]] = docs.groupBy(dt => DateTimeFormat.forPattern("yyyy-MM-dd").print(dt.sourceTime))

  def docsFromIds(ids: Seq[String]): Future[Seq[Trend]] = TrendRecord.Trends(ids).map(_.sortBy(_.score)(scala.math.Ordering.Float.reverse))

  def reduceOptionListToSingleOption(seqOptionalLong: Set[Option[Long]]): Long = seqOptionalLong.map(_.fold(0L)(x => x)).foldLeft(0L)(_ + _)

  def add(doc: Trend): Future[Long]
}

object QueryByDateRange extends QueryBy{
  def add(doc: Trend): Future[Long] = Future{redisConnectionPool.withClient(_.zadd(query_key, doc.sourceTime.getMillis / 1000, doc.id))}.map(_.fold(0L)(x => x))
  lazy val query_key = "QueryByDateRangeKey"

  def TrendIds(start: DateTime, end: DateTime): Future[Seq[String]] = Future{
    val idsWithScore: Option[List[String]] = redisConnectionPool.withClient(_.zrangebyscore(query_key, start.getMillis / 1000, true, end.getMillis / 1000, true, None))
    idsWithScore.toSeq.flatten
  }

  def trends(start: DateTime, end: DateTime): Future[Map[String, Seq[Trend]]] = {
    for {
      dIds <- TrendIds(start, end)
      docs <- docsFromIds(dIds)
    } yield(groupByDate(docs))
  }
}

object QueryBySource extends QueryBy{
  def add(doc: Trend): Future[Long] = Future{redisConnectionPool.withClient(_.sadd(query_key(doc.source), doc.id))}.map(_.fold(0L)(x => x))
  def query_key(source: String): String = s"QueryBySource:${source}"

  def TrendIds(source: String): Future[Seq[String]] = Future{
    val idsWithScore: Option[Set[Option[String]]] = redisConnectionPool.withClient(_.smembers(query_key(source)))
    idsWithScore.toSeq.flatten.map(_.toList).flatten
  }

  def trends(source: String): Future[Map[String, Seq[Trend]]] = {
    for {
      dIds <- TrendIds(source)
      docs <- docsFromIds(dIds)
    } yield(groupByDate(docs))
  }
}

object QueryByCategories extends QueryBy{
  def add(doc: Trend): Future[Long] = {
    val addsInFuture = doc.categories.map(v => Future{redisConnectionPool.withClient(_.sadd(query_key(v), doc.id))})
    Future.sequence(addsInFuture).map(reduceOptionListToSingleOption)
  }
  def query_key(vertical: String): String = s"QueryByCategory:${vertical}"

  def TrendIds(categories: List[String]): Future[Seq[String]] = {
    val idsPerCategory:List[Future[Seq[String]]] = categories.map(vertical => {
      Future{
        val idsWithScore: Option[Set[Option[String]]] = redisConnectionPool.withClient(_.smembers(query_key(vertical)))
        idsWithScore.toSeq.flatten.map(_.toList).flatten
      }
    })
    Future.sequence(idsPerCategory).map(_.flatten)
  }

  def trends(categories: String): Future[Map[String, Seq[Trend]]] = {
    for {
      dIds <- TrendIds(categories.split(",").toList)
      docs <- docsFromIds(dIds)
    } yield(groupByDate(docs))
  }
}

object QueryByDateAndSource extends QueryBy{
  def add(doc: Trend): Future[Long] = Future{redisConnectionPool.withClient(_.zadd(query_key(doc.source), doc.sourceTime.getMillis / 1000, doc.id))}.map(_.fold(0L)(x => x))
  def query_key(source: String): String = s"QueryByDateAndSource:${source}"

  def TrendIds(start: DateTime, end: DateTime, source: String): Future[Seq[String]] = Future{
    val idsWithScore: Option[List[String]] = redisConnectionPool.withClient(_.zrangebyscore(query_key(source), start.getMillis / 1000, true, end.getMillis / 1000, true, None))
    idsWithScore.toSeq.flatten
  }

  def trends(start: DateTime, end: DateTime, source: String): Future[Map[String, Seq[Trend]]] = {
    for {
      dIds <- TrendIds(start, end, source)
      docs <- docsFromIds(dIds)
    } yield(groupByDate(docs))
  }
}

object QueryByCategoriesAndSource extends QueryBy{
  def add(doc: Trend): Future[Long] = {
    val addsInFuture = doc.categories.map(v => Future{redisConnectionPool.withClient(_.sadd(query_key(v, doc.source), doc.id))})
    Future.sequence(addsInFuture).map(reduceOptionListToSingleOption)
  }
  def query_key(vertical: String, source: String): String = s"QueryByCategoryAndSource:${vertical}:${source}"

  def TrendIds(categories: List[String], source: String): Future[Seq[String]] = {
    val idsPerCategory = categories.map(vertical => {
      Future{
        val idsWithScore: Option[Set[Option[String]]] = redisConnectionPool.withClient(_.smembers(query_key(vertical, source)))
        idsWithScore.toSeq.flatten.map(_.toList).flatten
      }
    })
    Future.sequence(idsPerCategory).map(_.flatten)
  }

  def trends(categories: String, source: String): Future[Map[String, Seq[Trend]]] = {
    for {
      dIds <- TrendIds(categories.split(",").toList, source)
      docs <- docsFromIds(dIds)
    } yield(groupByDate(docs))
  }
}

object QueryByDateAndCategories extends QueryBy{
  def add(doc: Trend): Future[Long] = {
    val addsInFuture = doc.categories.map(v => Future{redisConnectionPool.withClient(_.zadd(query_key(v), doc.sourceTime.getMillis / 1000, doc.id))})
    Future.sequence(addsInFuture).map(reduceOptionListToSingleOption)
  }
  def query_key(vertical: String): String = s"QueryByDateAndCategory:${vertical}"

  def TrendIds(start: DateTime, end: DateTime, categories: List[String]): Future[Seq[String]] = {
    val idsPerCategory = categories.map(vertical => {
      Future{
        val idsWithScore: Option[List[String]] = redisConnectionPool.withClient(_.zrangebyscore(query_key(vertical), start.getMillis / 1000, true, end.getMillis / 1000, true, None))
        idsWithScore.toSeq.flatten
      }
    })
    Future.sequence(idsPerCategory).map(_.flatten)
  }

  def trends(start: DateTime, end: DateTime, categories: String): Future[Map[String, Seq[Trend]]] = {
    for {
      dIds <- TrendIds(start, end, categories.split(",").toList)
      docs <- docsFromIds(dIds)
    } yield(groupByDate(docs))
  }
}

object QueryByDateAndCategoriesAndSource extends QueryBy{
  def add(doc: Trend): Future[Long] = {
    val addsInFuture = doc.categories.map(v => Future{redisConnectionPool.withClient(_.zadd(query_key(v, doc.source), doc.sourceTime.getMillis / 1000, doc.id))})
    Future.sequence(addsInFuture).map(reduceOptionListToSingleOption)
  }
  def query_key(vertical: String, source: String): String = s"QueryByDateAndCategoryAndSource:${vertical}:${source}"

  def TrendIds(start: DateTime, end: DateTime, categories: List[String], source: String): Future[Seq[String]] = {
    val idsPerCategory = categories.map(vertical => {
      Future{
        val idsWithScore: Option[List[String]] = redisConnectionPool.withClient(_.zrangebyscore(query_key(vertical, source), start.getMillis / 1000, true, end.getMillis / 1000, true, None))
        idsWithScore.toSeq.flatten
      }
    })
    Future.sequence(idsPerCategory).map(_.flatten)
  }

  def trends(start: DateTime, end: DateTime,  source: String,categories: String): Future[Map[String, Seq[Trend]]] = {
    for {
      dIds <- TrendIds(start, end, categories.split(",").toList, source)
      docs <- docsFromIds(dIds)
    } yield(groupByDate(docs))
  }
}


