package es.eriktorr.lambda4s

import munit.Tag

object TestFilters:
  val envVars: Tag = Tag("envVars")
  val online: Tag = Tag("online")

  val envVarsName: String = "TEST_ENV_VARS"
