package es.eriktorr.lambda4s
package database

import ciris.Secret
import com.comcast.ip4s.{Host, Port}

final case class DatabaseConfiguration(
    database: String,
    host: Host,
    password: Secret[String],
    port: Port,
    user: String,
)
