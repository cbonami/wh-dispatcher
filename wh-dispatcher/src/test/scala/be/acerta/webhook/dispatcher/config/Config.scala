package be.acerta.webhook.dispatcher.config

import java.util.{Date, Random}
import scala.concurrent.duration._

object Config {

  val app_url = "http://localhost:8080"

  val now = new Date
  val duration = System.getProperty("duration").toInt
  val numPause = java.lang.Long.getLong("pause", 3L)
  val testPause = Duration(numPause, "seconds")

}
