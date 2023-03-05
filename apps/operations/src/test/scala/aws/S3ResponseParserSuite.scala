package es.eriktorr.lambda4s
package aws

final class S3ResponseParserSuite {}

object S3ResponseParserSuite:
  private val listObjectsV2Response =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
      |  <Name>quotes</Name>
      |  <Prefix>E</Prefix>
      |  <StartAfter>ExampleGuide.pdf</StartAfter>
      |  <KeyCount>1</KeyCount>
      |  <MaxKeys>3</MaxKeys>
      |  <IsTruncated>false</IsTruncated>
      |  <Contents>
      |    <Key>ExampleObject.txt</Key>
      |    <LastModified>2013-09-17T18:07:53.000Z</LastModified>
      |    <ETag>"599bab3ed2c697f1d26842727561fd94"</ETag>
      |    <Size>857</Size>
      |    <StorageClass>REDUCED_REDUNDANCY</StorageClass>
      |  </Contents>
      |</ListBucketResult>""".stripMargin
