package observatory

import com.sksamuel.scrimage.writer
import org.scalameter.{Key, Warmer, config}

object Main {

  val mcfg = config(
    Key.verbose -> true,
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 30
  ) withWarmer (new Warmer.Default)

  val stationsPath = "/stations.csv"
  val temperaturesPath = "/1975.csv"
  val year = 1975

  // val data1 = Extraction.locateTemperatures(year, stationsPath, temperaturesPath)
  // val data2 = Extraction.locationYearlyAverageRecords(data1)

  val te = Seq(
    (Location(45.0, -90.0), 20.0),
    (Location(45.0, 90.0), 0.0),
    (Location(0.0, 0.0), 10.0),
    (Location(-45.0, -90.0), 0.0),
    (Location(-45.0, 90.0), 20.0)
  )

  val co = List(
    (0.0, Color(255, 0, 0)),
    (10.0, Color(0, 255, 0)),
    (20.0, Color(0, 0, 255))
  )

  val ti = Tile(0, 0, 0)

  def main(args: Array[String]): Unit = {
    val image = Interaction.tile(te, co, ti)
    image.output("256x256-testData.png")
  }
}
