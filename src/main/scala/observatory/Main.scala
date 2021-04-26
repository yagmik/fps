package observatory

import org.scalameter.{Key, Warmer, config, measure}
import com.sksamuel.scrimage.{Image, writer}

object Main {

  val mcfg = config(
    Key.verbose -> true,
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 30
  ) withWarmer (new Warmer.Default)

  val stationsPath = "/stations.csv"
  val temperaturesPath = "/2015.csv"
  val year = 2015

  val data1 = Extraction.locateTemperatures(year, stationsPath, temperaturesPath)
  val data2 = Extraction.locationYearlyAverageRecords(data1)

  def main(args: Array[String]): Unit = {
    var image: Image = null
    val c = measure {
      image = Visualization.visualize(data2, colors)
    }
    image.output("360x180.png")
    println(s"c = ${c.toString()}")
  }
}
