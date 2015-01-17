package com.myweb.apple.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
object TimeHelper {

  def getTimeStamp(timeString: String): Long = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").parseDateTime(timeString).getMillis

  def getLongTimeOfDate(date: String): Long = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(date).getMillis / 1000

  def getDateString(time: Long): String = DateTimeFormat.forPattern("yyyy-MM-dd").print(time * 1000)
}
