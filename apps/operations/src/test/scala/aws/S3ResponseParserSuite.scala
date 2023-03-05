package es.eriktorr.lambda4s
package aws

import aws.S3ResponseParserSuite.{listObjectsV2OutputResponse, listObjectsV2Response}

import cats.effect.IO
import fs2.Stream
import fs2.data.xml.xpath.filter
import fs2.data.xml.xpath.literals.xpath
import fs2.data.xml.{collector, events}
import munit.CatsEffectSuite

final class S3ResponseParserSuite extends CatsEffectSuite:
  test("should parse s3 list objects v2 response using default provider") {
    testWith(listObjectsV2Response)
  }

  test("should parse s3 list objects v2 response using asf provider") {
    testWith(listObjectsV2OutputResponse)
  }

  private def testWith(xml: String) =
    (for keysFound <- Stream
        .emit(xml)
        .through(events[IO, String]())
        .through(filter.collect(xpath"/*/Contents/Key", collector.show, deterministic = false))
        .compile
        .count
    yield keysFound).assertEquals(1L)

object S3ResponseParserSuite:
  private val listObjectsV2Response =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
      |  <Name>test-bucket</Name>
      |  <Prefix>test-object-key</Prefix>
      |  <MaxKeys>1</MaxKeys>
      |  <KeyCount>1</KeyCount>
      |  <IsTruncated>false</IsTruncated>
      |  <Contents>
      |    <Key>test-object-key</Key>
      |    <LastModified>2023-03-05T16:27:45.000Z</LastModified>
      |    <ETag>&#34;6f5902ac237024bdd0c176cb93063dc4&#34;</ETag>
      |    <Size>12</Size>
      |    <StorageClass>STANDARD</StorageClass>
      |  </Contents>
      |  <Marker></Marker>
      |</ListBucketResult>""".stripMargin

  private val listObjectsV2OutputResponse =
    """<?xml version='1.0' encoding='utf-8'?>
      |<ListObjectsV2Output>
      |  <IsTruncated>false</IsTruncated>
      |  <Contents>
      |    <Key>test-object-key</Key>
      |    <LastModified>2023-03-05T13:51:48Z</LastModified>
      |    <ETag>"6f5902ac237024bdd0c176cb93063dc4"</ETag>
      |    <Size>12</Size>
      |    <StorageClass>STANDARD</StorageClass>
      |  </Contents>
      |  <Name>test-bucket</Name>
      |  <Prefix>test-object-key</Prefix>
      |  <MaxKeys>1</MaxKeys>
      |  <KeyCount>1</KeyCount>
      |</ListObjectsV2Output>""".stripMargin
