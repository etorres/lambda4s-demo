package es.eriktorr.lambda4s

import TestFilters.envVars
import slack.SlackConfiguration

import java.net.URI

final class CloudWatchEventsConfigurationSuite extends CatsEffectSuiteWithEnvironment:
  test("should read configuration from environment".tag(envVars)) {
    CloudWatchEventsConfiguration.load.assertEquals(
      CloudWatchEventsConfiguration(SlackConfiguration(URI.create("https://example.org").nn)),
    )
  }
