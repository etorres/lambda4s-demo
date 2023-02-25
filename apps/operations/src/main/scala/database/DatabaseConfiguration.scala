package es.eriktorr.lambda4s
package database

import com.comcast.ip4s.{Host, Port}

final case class DatabaseConfiguration(
    database: String,
    host: Host,
    password: String,
    port: Port,
    user: String,
)
