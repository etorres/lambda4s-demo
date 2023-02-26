package es.eriktorr.lambda4s

import TestFilters.{envVars, envVarsName}

import munit.CatsEffectSuite

abstract class CatsEffectSuiteWithEnvironment extends CatsEffectSuite:
  override def munitTests(): Seq[Test] =
    val default = super.munitTests()
    if Environment.envOrNone(envVarsName).isEmpty then default.filterNot(_.tags.contains(envVars))
    else default
