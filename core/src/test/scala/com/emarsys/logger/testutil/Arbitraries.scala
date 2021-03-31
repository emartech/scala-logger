package com.emarsys.logger.testutil

import org.scalacheck.Gen

import java.time._
import java.time.temporal.ChronoUnit.MILLIS
import scala.jdk.CollectionConverters._

object Arbitraries {

  implicit val zonedDateTimeGen: Gen[ZonedDateTime] =
    for {
      year  <- Gen.choose(-292278994, 292278994)
      month <- Gen.choose(1, 12)
      maxDaysInMonth = Month.of(month).length(Year.of(year).isLeap)
      dayOfMonth   <- Gen.choose(1, maxDaysInMonth)
      hour         <- Gen.choose(0, 23)
      minute       <- Gen.choose(0, 59)
      second       <- Gen.choose(0, 59)
      nanoOfSecond <- Gen.choose(0, 999999999)
      zoneId       <- Gen.oneOf(ZoneId.getAvailableZoneIds.asScala.toList)
    } yield ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, ZoneId.of(zoneId))

  implicit val instantGen: Gen[Instant]               = zonedDateTimeGen.map(_.toInstant)
  implicit val localDateTimeGen: Gen[LocalDateTime]   = zonedDateTimeGen.map(_.toLocalDateTime)
  implicit val localDateGen: Gen[LocalDate]           = zonedDateTimeGen.map(_.toLocalDate)
  implicit val localTimeGen: Gen[LocalTime]           = zonedDateTimeGen.map(_.toLocalTime)
  implicit val offsetDateTimeGen: Gen[OffsetDateTime] = zonedDateTimeGen.map(_.toOffsetDateTime)
  implicit val offsetTimeGen: Gen[OffsetTime]         = zonedDateTimeGen.map(_.toOffsetDateTime.toOffsetTime)

  implicit val durationGen: Gen[Duration] = Gen
    .choose(Long.MinValue, Long.MaxValue / 1000)
    .map(l => Duration.of(l, MILLIS))

}
