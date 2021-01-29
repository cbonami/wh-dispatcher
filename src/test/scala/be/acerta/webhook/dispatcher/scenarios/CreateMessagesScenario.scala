package be.acerta.webhook.dispatcher.scenarios

import scala.concurrent.duration._
import java.util.{Date, Random}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import be.acerta.webhook.dispatcher.config.Config.app_url
import be.acerta.webhook.dispatcher.config.Config.duration

object CreateMessagesScenario {

  object randomStringGenerator {
    def randomString(length: Int) = scala.util.Random.alphanumeric.filter(_.isLetter).take(length).mkString
	}
	
	val now = new Date
  val duration = System.getProperty("duration").toInt
  val numPause = java.lang.Long.getLong("pause", 3L)
  val testPause = Duration(numPause, "seconds")

  val body = """{"type":"webhook_v1", "body":"0000000000"}"""

	var randomBody = Iterator.continually(Map("randBody" -> ( body.replace("0000000000", randomStringGenerator.randomString(10)))))

	val headers_post = Map(
		"Content-Type" -> "application/json",
		"Origin" -> app_url,
    "X-Requested-With" -> "XMLHttpRequest")
    
  val createMessagesScenario = scenario("messages.create")
		.during(duration seconds){
			feed(randomBody)
			.exec(
				http("post_message")
					.post("/api/applications/8aec6535-f23d-48f2-b27b-c43ae6b9c906/messages")
					.headers(headers_post)
					.body(StringBody( """${randBody}"""))
					.check(status.is(201))
			)
		}
}
