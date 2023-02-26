package es.eriktorr.lambda4s

import scala.scalajs.js
import scala.util.control.NonFatal

object Environment:
  def envOrNone(name: String): Option[String] =
    try
      val env = js.Dynamic.global.process.env.asInstanceOf[js.Dictionary[Any]]
      env.get(name).map(_.asInstanceOf[Matchable]).collect { case value: String => value }
    catch case e if NonFatal(e) => None
