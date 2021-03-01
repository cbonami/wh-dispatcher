package be.acerta.webhook.dispatcher.simulations

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import be.acerta.webhook.dispatcher.config.Config._
import be.acerta.webhook.dispatcher.scenarios.CreateMessagesScenario

class LoadTest extends Simulation {

	val httpProtocol = http
		.baseUrl(app_url)
		.inferHtmlResources(BlackList(), WhiteList())
		.acceptHeader("application/hal+json, application/json, */*; q=0.01")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.userAgentHeader("curl/7.54.0")

  private val createMessagesScenario = CreateMessagesScenario.createMessagesScenario

  setUp(createMessagesScenario.inject(atOnceUsers(1))).protocols(httpProtocol)

}
