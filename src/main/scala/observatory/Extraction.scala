package observatory

import java.time.LocalDate

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface with Extractor {

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year,
                         stationsFile: String,
                         temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] =
    readTemperatures(temperaturesFile, readStations(stationsFile), year)

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    records
      .groupBy(_._2)
      .mapValues { it =>
        val p = it.foldLeft((0d, 0))((acc, t) => (acc._1 + t._3, acc._2 + 1))
        p._1 / p._2
      }
      .toSeq
  }
}
