package com.myweb.apple

import com.typesafe.config._

object AppConfig {
  val env = sys.env.get("APPLE_ENV").getOrElse("development")
  lazy val conf = ConfigFactory.load(env)
}
