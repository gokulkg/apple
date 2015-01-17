package com.myweb.apple.util

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLDataException
import java.sql.SQLFeatureNotSupportedException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLNonTransientException
import java.sql.SQLRecoverableException
import java.sql.SQLSyntaxErrorException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.Properties
import java.io.FileInputStream
import scala.collection.mutable.ArrayBuffer
import org.slf4j.LoggerFactory
import com.mysql.jdbc.log.Log
import com.sun.org.apache.xml.internal.serializer.ToStream
import java.sql.Timestamp

final class MysqlClient {

  final case class MySQLConnection(server: String, connection: Connection) {
    def prepareStatement(sql: String) = connection.prepareStatement(sql)
    def close = connection.close
  }
  // loading mysql driver
  Class.forName("com.mysql.jdbc.Driver").newInstance

  // check if loaded
  import MysqlClient._
  ensureDriverLoaded

  val log = LoggerFactory.getLogger("MysqlClient")
  val env = sys.env.get("JIMI_ENV").getOrElse("development")
  val (host: String, port: Integer, db: String, user: String, password: String) =
    try {
      val prop = new Properties()
      prop.load(new FileInputStream(s"src/main/resources/${env}/mysql.properties"))

      (
        prop.getProperty("host").toString,
        new Integer(prop.getProperty("port")),
        prop.getProperty("db"),
        prop.getProperty("user"),
        prop.getProperty("password"))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        sys.exit(1)
    }

  def execute[T](f: ResultSet => T, sql: String, params: Seq[Any]): Seq[T] = {

    var connection = newConnection(host, port, db, user, password)
    if (connection == null) {
      val e = new IllegalStateException("WTF?  Couldn't get a connection from the pool.")
      log.error(e.getMessage)
      throw e
    }
    try {
      val statement = connection.prepareStatement(sql)
      try {
        if (params.size > 0)
          bindParameters(statement, params)
        log.debug(connection.server + ": " + sql
          + " " + params.mkString("(", ", ", ")"))
        val rs = statement.executeQuery
        try {
          val results = new ArrayBuffer[T]
          while (rs.next) {
            results += f(rs)
          }
          results
        } finally {
          rs.close
        }
      } finally {
        statement.close
      }
    } catch {
      case e: Throwable => logAndRethrow(e)
    }
  }

  def update(sql: String, params: Seq[Any]): Int = {

    var connection = newConnection(host, port, db, user, password)
    if (connection == null) {
      val e = new IllegalStateException("WTF?  Couldn't get a connection from the pool.")
      log.error(e.getMessage)
      throw e
    }
    try {
      val statement = connection.prepareStatement(sql)
      try {
        if (params.size > 0)
          bindParameters(statement, params)
        log.debug(connection.server + ": " + sql
          + " " + params.mkString("(", ", ", ")"))
        val rs = statement.executeUpdate()
        rs
      } finally {
        statement.close
      }
    } catch {
      case e: Throwable => logAndRethrow(e)
    }
  }

  private def logAndRethrow(e: Throwable) = {

    val cause = new StringBuilder
    var exception = e
    // Get names & messages of all exceptions in the chain.
    while (exception != null) {
      cause.append(", caused by ")
        .append(e.getClass.getName)
        .append(": ")
        .append(e.getMessage)

      exception = exception.getCause
    }
    log.error(cause.toString)
    throw e
  }

  private def bindParameters(statement: PreparedStatement,
                             params: TraversableOnce[Any]) {
    bindParameters(statement, 1, params)
  }

  private def bindParameters(statement: PreparedStatement,
                             startIndex: Int,
                             params: TraversableOnce[Any]): Int = {
    var index = startIndex
    for (param <- params) {
      param match {
        case i: Int    => statement.setInt(index, i)
        case l: Long   => statement.setLong(index, l)
        case s: String => statement.setString(index, s)
        case l: TraversableOnce[_] =>
          index = bindParameters(statement, index, l) - 1
        case p: Product =>
          index = bindParameters(statement, index, p.productIterator.toList) - 1
        case b: Array[Byte] => statement.setBytes(index, b)
        case b: Boolean     => statement.setBoolean(index, b)
        case s: Short       => statement.setShort(index, s)
        case f: Float       => statement.setFloat(index, f)
        case d: Double      => statement.setDouble(index, d)
        case t: Timestamp   => statement.setTimestamp(index, t)
        case _ =>
          throw new IllegalArgumentException("Unsupported data type "
            + param.asInstanceOf[AnyRef].getClass.getName + ": " + param)
      }
      index += 1
    }
    index
  }

  private def newConnection(host: String, port: Int, db: String, user: String, password: String): MySQLConnection = {
    val connection =
      DriverManager.getConnection(s"jdbc:mysql://${host}:${port}/${db}",
        user, password)
    MySQLConnection(host, connection)
  }
}

object MysqlClient {
  private def ensureDriverLoaded =
    if (classOf[com.mysql.jdbc.Driver] == null) {
      throw new AssertionError("MySQL JDBC connector missing.")
    }
}

