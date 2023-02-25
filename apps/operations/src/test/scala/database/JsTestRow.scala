package es.eriktorr.lambda4s
package database

import org.scalablytyped.runtime.StObject

import scala.scalajs.js

@js.native
trait JsTestRow extends StObject:
  var name: String = js.native
  var value: Int = js.native
  var price: Double = js.native

object JsTestRow:
  @scala.inline
  def apply(name: String, value: Int, price: Double): JsTestRow =
    val __obj = js.Dynamic.literal(
      name = name.asInstanceOf[js.Any],
      value = value.asInstanceOf[js.Any],
      price = price.asInstanceOf[js.Any],
    )
    __obj.asInstanceOf[JsTestRow]
