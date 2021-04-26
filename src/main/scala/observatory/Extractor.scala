package observatory

import java.time.LocalDate
import scala.io.Source

trait Extractor {
  def using[T](path: String)(f: Iterator[String] => T): T = {
    val source = Source.fromInputStream(getClass.getResourceAsStream(path))
    try {
      f(source.getLines())
    } finally {
      source.close()
    }
  }

  def validStationRow(srow: Array[String]): Boolean =
    srow.length == 4 && srow(2).nonEmpty && srow(3).nonEmpty

  def createStationRecord(srow: Array[String]): (StationId, Location) =
    (StationId(srow(0), srow(1)), Location(srow(2).toDouble, srow(3).toDouble))

  def parseStations(it: Iterator[String]): Map[StationId, Location] =
    it
      .toIndexedSeq
      .par
      .map(_.split(','))
      .filter(validStationRow)
      .map(createStationRecord)
      .toMap
      .seq

  def readStations(stationsPath: String): Map[StationId, Location] =
    using(stationsPath)(parseStations)

  def validTemperatureRow(stations: Map[StationId, Location])(trow: Array[String]): Boolean =
    stations.contains(StationId(trow(0), trow(1)))

  def createTemperatureRecord(stations: Map[StationId, Location], year: Year)
                             (trow: Array[String]): (LocalDate, Location, Temperature) = {
    val localDate = LocalDate.of(year, trow(2).toInt, trow(3).toInt)
    val location = stations(StationId(trow(0), trow(1)))
    val temperature: Temperature = fahrenheitToCelsius(trow(4).toDouble)
    (localDate, location, temperature)
  }

  val fahrenheitToCelsius: Temperature => Temperature =
    (fahrenheit: Temperature) => (fahrenheit - 32) * 5 / 9

  def parseTemperaturesSeq(stations: Map[StationId, Location], year: Year)
                          (it: Iterator[String]): Iterable[(LocalDate, Location, Temperature)] =
    it
      .map(_.split(','))
      .filter(validTemperatureRow(stations))
      .map(createTemperatureRecord(stations, year))
      .toIndexedSeq

  def readTemperatures(temperaturesPath: String,
                       stations: Map[StationId, Location],
                       year: Year): Iterable[(LocalDate, Location, Temperature)] =
    using(temperaturesPath)(parseTemperaturesSeq(stations, year))
}

object Extractor extends Extractor
