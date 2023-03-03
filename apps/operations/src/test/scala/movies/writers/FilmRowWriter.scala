package es.eriktorr.lambda4s
package movies.writers

import StringGenerators.stringBetween
import TemporalGenerators.localDateGen
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite, RowWriter}
import movies.Rating
import movies.writers.FilmRowWriter.FilmRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class FilmRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[FilmRow](testTransactor)
    with RowWriter[FilmRow]:
  override def add(rows: List[FilmRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO film (
              | film_id,
              | title,
              | description,
              | release_year,
              | language_id,
              | original_language_id,
              | rental_duration,
              | rental_rate,
              | length,
              | replacement_cost,
              | rating,
              | special_features
              |) VALUES (
              | ${row.film_id},
              | '${row.title}',
              | ${row.description.fold("NULL")(x => s"'$x'")},
              | ${row.release_year},
              | ${row.language_id},
              | ${row.original_language_id.getOrElse("NULL")},
              | ${row.rental_duration},
              | '${row.rental_rate}',
              | ${row.length.getOrElse("NULL")},
              | '${row.replacement_cost}',
              | '${row.rating.name}',
              | ${row.special_features.fold("NULL")(x => s"'$x'")}
              |)""".stripMargin,
  )

object FilmRowWriter:
  final case class FilmRow(
      film_id: Short,
      title: String,
      description: Option[String],
      release_year: Short,
      language_id: Byte,
      original_language_id: Option[Byte],
      rental_duration: Byte,
      rental_rate: Double,
      length: Option[Short],
      replacement_cost: Double,
      rating: Rating,
      special_features: Option[String],
      last_update: Option[LocalDateTime],
  )

  val filmIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  def filmRowGen(filmIdGen: Gen[Short], languageIdGen: Gen[Byte]): Gen[FilmRow] = for
    film_id <- filmIdGen
    title <- stringBetween(3, 128)
    release_year <- localDateGen.map(_.getYear.toShort)
    language_id <- languageIdGen
    rental_duration <- Gen.choose[Byte](0, 10)
    rental_rate <- Gen.choose(1.0d, 5.0d)
    replacement_cost <- Gen.choose(5.0d, 20.0d)
    rating <- Gen.oneOf(Rating.values.toList)
  yield FilmRow(
    film_id,
    title,
    None,
    release_year,
    language_id,
    None,
    rental_duration,
    rental_rate,
    None,
    replacement_cost,
    rating,
    None,
    None,
  )
