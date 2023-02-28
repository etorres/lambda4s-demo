package es.eriktorr.lambda4s
package movies

import CollectionGenerators.nDistinct
import TemporalGenerators.{localDateTimeRangeGen, outOfLocalDateTimeRange, withinLocalDateTimeRange}
import database.mysql.MySqlTransactor
import infrastructure.DatabaseTestConfiguration.SakilaMySqlTest
import infrastructure.{DatabaseTestConfiguration, MySqlSuite, MySqlTestTransactor}
import movies.AddressRowWriter.{AddressRow, addressIdGen, addressRowGen}
import movies.CityRowWriter.{CityRow, cityIdGen, cityRowGen}
import movies.CountryRowWriter.{CountryRow, countryIdGen, countryRowGen}
import movies.CustomerRowWriter.{CustomerRow, customerIdGen, customerRowGen}
import movies.FilmRowWriter.{FilmRow, filmIdGen, filmRowGen}
import movies.InventoryRowWriter.{InventoryRow, inventoryIdGen, inventoryRowGen}
import movies.LanguageRowWriter.{LanguageRow, languageIdGen, languageRowGen}
import movies.MoviesReader.CumulativeRevenue
import movies.MoviesReaderSuite.cumulativeRevenueTestCaseGen
import movies.PaymentRowWriter.{PaymentRow, paymentIdGen, paymentRowGen}
import movies.RentalRowWriter.{RentalRow, rentalIdGen, rentalRowGen}
import movies.StaffRowWriter.{StaffRow, staffIdGen, staffRowGen}
import movies.StoreRowWriter.{StoreRow, storeIdGen, storeRowGen}

import cats.Applicative
import cats.implicits.toTraverseOps
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.*
import org.scalacheck.effect.PropF.forAllF

import java.time.{LocalDate, LocalDateTime}

final class MoviesReaderSuite extends MySqlSuite:

  test("should count the number of films grouped by their rating".ignore) {
    fail("not implemented")
  }

  test("should calculate the cumulative revenue during the given period") {
    forAllF(cumulativeRevenueTestCaseGen) { testCase =>
      MySqlTestTransactor
        .of(SakilaMySqlTest, munitExecutionContext)
        .use(testTransactor =>
          for
            logger <- Log4sFactory.impl().create
            _ <- CountryRowWriter(testTransactor).add(testCase.countryRows)
            _ <- CityRowWriter(testTransactor).add(testCase.cityRows)
            _ <- AddressRowWriter(testTransactor).add(testCase.addressRows)
            _ <- for
              _ <- testTransactor.update("SET FOREIGN_KEY_CHECKS=0")
              _ <- StaffRowWriter(testTransactor).add(testCase.staffRows)
              _ <- testTransactor.update("SET FOREIGN_KEY_CHECKS=1")
            yield ()
            _ <- StoreRowWriter(testTransactor).add(testCase.storeRows)
            _ <- CustomerRowWriter(testTransactor).add(testCase.customerRows)
            _ <- LanguageRowWriter(testTransactor).add(testCase.languageRows)
            _ <- FilmRowWriter(testTransactor).add(testCase.filmRows)
            _ <- InventoryRowWriter(testTransactor).add(testCase.inventoryRows)
            _ <- RentalRowWriter(testTransactor).add(testCase.rentalRows)
            _ <- PaymentRowWriter(testTransactor).add(testCase.paymentRows)
            moviesReader = MoviesReader.impl(testTransactor.transactor)(using logger)
            obtained <- moviesReader.cumulativeRevenueDuring(testCase.dateTimeRange)
          yield obtained,
        )
        .assertEquals(testCase.expected, "Cumulative revenues are not the same")
    }
  }

object MoviesReaderSuite:
  final private case class CumulativeRevenueTestCase(
      addressRows: List[AddressRow],
      cityRows: List[CityRow],
      countryRows: List[CountryRow],
      customerRows: List[CustomerRow],
      dateTimeRange: DateRange[LocalDateTime],
      expected: List[CumulativeRevenue],
      filmRows: List[FilmRow],
      inventoryRows: List[InventoryRow],
      languageRows: List[LanguageRow],
      paymentRows: List[PaymentRow],
      rentalRows: List[RentalRow],
      staffRows: List[StaffRow],
      storeRows: List[StoreRow],
  )

  private val cumulativeRevenueTestCaseGen = for
    addressIds <- nDistinct(3, addressIdGen)
    countryIds <- nDistinct(3, countryIdGen)
    cityIds <- nDistinct(3, cityIdGen)
    customerIds <- nDistinct(3, customerIdGen)
    dateTimeRange <- localDateTimeRangeGen
    filmIds <- nDistinct(3, filmIdGen)
    inventoryIds <- nDistinct(3, inventoryIdGen)
    languageIds <- nDistinct(3, languageIdGen)
    paymentIds <- nDistinct(7, paymentIdGen)
    rentalIds <- nDistinct(7, rentalIdGen)
    managersWithStore <- nDistinct(3, Applicative[Gen].product(staffIdGen, storeIdGen))
    staffIds = managersWithStore.map(_._1)
    storeIds = managersWithStore.map(_._2)
    countries <- countryIds.traverse(countryRowGen(_))
    cities <- cityIds.traverse(cityRowGen(_, Gen.oneOf(countryIds)))
    addresses <- addressIds.traverse(addressRowGen(_, Gen.oneOf(cityIds)))
    staff <- staffIds.traverse(staffRowGen(Gen.oneOf(addressIds), _, Gen.oneOf(storeIds)))
    stores <- managersWithStore.traverse { case (staffId, storeId) =>
      storeRowGen(storeId, staffId, Gen.oneOf(addressIds))
    }
    customers <- customerIds.traverse(customerRowGen(Gen.oneOf(addressIds), _, Gen.oneOf(storeIds)))
    languages <- languageIds.traverse(languageRowGen(_))
    films <- filmIds.traverse(filmRowGen(_, Gen.oneOf(languageIds)))
    inventory <- inventoryIds.traverse(inventoryRowGen(Gen.oneOf(filmIds), _, Gen.oneOf(storeIds)))
    rentals <- rentalIds.traverse(
      rentalRowGen(Gen.oneOf(customerIds), Gen.oneOf(inventoryIds), _, Gen.oneOf(staffIds)),
    )
    (selectedPaymentIds, paymentIdsOutOfDateTimeRange) = paymentIds.splitAt(3)
    selectedPayments <- selectedPaymentIds.traverse(paymentId =>
      paymentRowGen(
        customerIdGen = Gen.oneOf(customerIds),
        paymentDateGen = withinLocalDateTimeRange(dateTimeRange),
        paymentIdGen = paymentId,
        rentalIdGen = Gen.oneOf(rentalIds),
        staffIdGen = Gen.oneOf(staffIds),
      ),
    )
    paymentsOutOfDateTimeRange <- paymentIdsOutOfDateTimeRange.traverse(paymentId =>
      paymentRowGen(
        customerIdGen = Gen.oneOf(customerIds),
        paymentDateGen = outOfLocalDateTimeRange(dateTimeRange),
        paymentIdGen = paymentId,
        rentalIdGen = Gen.oneOf(rentalIds),
        staffIdGen = Gen.oneOf(staffIds),
      ),
    )
    expected = List.empty
  yield CumulativeRevenueTestCase(
    addresses,
    cities,
    countries,
    customers,
    dateTimeRange,
    expected,
    films,
    inventory,
    languages,
    selectedPayments ++ paymentsOutOfDateTimeRange,
    rentals,
    staff,
    stores,
  )
