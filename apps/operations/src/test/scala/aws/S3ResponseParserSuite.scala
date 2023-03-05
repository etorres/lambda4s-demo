package es.eriktorr.lambda4s
package aws

import aws.S3ResponseParserSuite.{
  listObjectsV2OutputResponse,
  listObjectsV2OutputResponseNoKeyFound,
  listObjectsV2Response,
  otherXml,
}

import munit.CatsEffectSuite

final class S3ResponseParserSuite extends CatsEffectSuite:
  test("should parse s3 list objects v2 response using default provider") {
    testWith(listObjectsV2Response, 1L)
  }

  test("should parse s3 list objects v2 response using asf provider") {
    testWith(listObjectsV2OutputResponse, 1L)
  }

  test("should not found any key in a valid document") {
    testWith(listObjectsV2OutputResponseNoKeyFound, 0L)
  }

  test("should not found any key in an unrelated document") {
    testWith(otherXml, 0L)
  }

  private def testWith(xml: String, expected: Long) =
    S3ResponseParser.keysFoundIn(xml, "test-object-key").assertEquals(expected)

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

  private val listObjectsV2OutputResponseNoKeyFound =
    """<?xml version='1.0' encoding='utf-8'?>
      |<ListObjectsV2Output>
      |  <IsTruncated>false</IsTruncated>
      |  <Contents>
      |    <Key>test-object-key0</Key>
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

  private val otherXml = """<?xml version="1.0" encoding="UTF-8"?>
                           |<bookstore>
                           |  <book>
                           |    <title lang="en">Harry Potter</title>
                           |    <author>J K. Rowling</author>
                           |    <year>2005</year>
                           |    <price>29.99</price>
                           |  </book>
                           |</bookstore>""".stripMargin
