package es.eriktorr.lambda4s
package movies

import StringGenerators.stringBetween
import TemporalGenerators.localDateGen
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite}
import movies.FilmRowWriter.FilmRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class FilmRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[FilmRow](testTransactor):
  def add(rows: List[FilmRow]): IO[Unit] = super.add(
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
              | '${row.rating}',
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
      rating: String,
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
    rating <- Gen.oneOf(Rating.values.toList).map(_.name)
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

/*
CREATE TABLE film (
  film_id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  description TEXT DEFAULT NULL,
  release_year YEAR DEFAULT NULL,
  language_id TINYINT UNSIGNED NOT NULL,
  original_language_id TINYINT UNSIGNED DEFAULT NULL,
  rental_duration TINYINT UNSIGNED NOT NULL DEFAULT 3,
  rental_rate DECIMAL(4,2) NOT NULL DEFAULT 4.99,
  length SMALLINT UNSIGNED DEFAULT NULL,
  replacement_cost DECIMAL(5,2) NOT NULL DEFAULT 19.99,
  rating ENUM('G','PG','PG-13','R','NC-17') DEFAULT 'G',
  special_features SET('Trailers','Commentaries','Deleted Scenes','Behind the Scenes') DEFAULT NULL,
  last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY  (film_id),
  KEY idx_title (title),
  KEY idx_fk_language_id (language_id),
  KEY idx_fk_original_language_id (original_language_id),
  CONSTRAINT fk_film_language FOREIGN KEY (language_id) REFERENCES language (language_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_film_language_original FOREIGN KEY (original_language_id) REFERENCES language (language_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 */
