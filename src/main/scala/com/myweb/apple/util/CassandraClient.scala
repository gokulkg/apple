package com.myweb.apple.util

import com.datastax.driver.core._
import scala.collection.JavaConversions._
import java.util.Properties
import java.io.FileInputStream
import com.myweb.apple._

case class CassandraClient(name: String) {
  lazy val host = AppConfig.conf.getString(s"${name}_cassandra.host")
  lazy val port = AppConfig.conf.getInt(s"${name}_cassandra.port")
  lazy val keyspace = AppConfig.conf.getString(s"${name}_cassandra.keyspace")
  val poolingOptions = new PoolingOptions
  poolingOptions.setMaxConnectionsPerHost(HostDistance.REMOTE, 32)
  lazy val cluster = Cluster.builder().addContactPoint(host).withPort(port).withPoolingOptions(poolingOptions).build();

  def execute(sql: String): ResultSet = {
    val session = cluster.connect(keyspace)
    session.execute(sql)
  }
}
