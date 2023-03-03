package es.eriktorr.lambda4s
package database

import org.scalablytyped.runtime.StObject

import scala.scalajs.js

@js.native
trait JsTestRow extends StObject:
  var address: String = js.native
  var zipcode: Int = js.native
  var price: Double = js.native
  var lastUpdate: js.Date = js.native
  var ranking: String = js.native

object JsTestRow:
  @scala.inline
  def apply(
      address: String,
      zipcode: Int,
      price: Double,
      lastUpdate: js.Date,
      ranking: String,
  ): JsTestRow =
    val __obj = js.Dynamic.literal(
      address = address.asInstanceOf[js.Any],
      zipcode = zipcode.asInstanceOf[js.Any],
      price = price.asInstanceOf[js.Any],
      lastUpdate = lastUpdate.asInstanceOf[js.Any],
      ranking = ranking.asInstanceOf[js.Any],
    )
    __obj.asInstanceOf[JsTestRow]
