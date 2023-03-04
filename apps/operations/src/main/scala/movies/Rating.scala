package es.eriktorr.lambda4s
package movies

/** Motion Picture Association film rating.
  * @see
  *   [[https://en.wikipedia.org/wiki/Motion_Picture_Association_film_rating_system Motion Picture Association film rating system]]
  */
enum Rating(val recommendedAge: Int, val name: String):
  case G extends Rating(0, "G")
  case PG extends Rating(8, "PG")
  case PG_13 extends Rating(13, "PG-13")
  case R extends Rating(18, "R")
  case NC_17 extends Rating(18, "NC-17")

object Rating:
  private def fromName(name: String): Option[Rating] = Rating.values.find(_.name == name)

  def fromNameOrFail(name: String): Rating = fromName(name).getOrElse(
    throw new IllegalArgumentException(s"No rating found with name: $name"),
  )
