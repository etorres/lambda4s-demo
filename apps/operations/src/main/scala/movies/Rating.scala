package es.eriktorr.lambda4s
package movies

/** Motion Picture Association film rating.
  * @see
  *   [[https://en.wikipedia.org/wiki/Motion_Picture_Association_film_rating_system Motion Picture Association film rating system]]
  */
enum Rating(val recommendedAge: Int):
  case G extends Rating(0)
  case PG extends Rating(8)
  case PG_13 extends Rating(13)
  case R extends Rating(18)
  case NC_17 extends Rating(18)
