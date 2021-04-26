package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.math.pow

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface with Distances {

  val w: Double => Double = (d: Double) => 1 / pow(d, DEFAULT_POWER_PARAMETER)

  def inverseDistanceWeighting(distances: Iterable[(Distance, Temperature)]): Temperature = {
    val minDistance = distances.minBy(_._1)
    if (minDistance._1 == 0) {
      minDistance._2
    } else {
      //            val (num, denom) = distances.aggregate((0d, 0d))(
      //              (acc, d) => (acc._1 + w(d._1) * d._2, acc._2 + w(d._1)),
      //              (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
      //            )
      //            num / denom
      val p = distances.foldLeft((0d, 0d))((acc, d) => {
        val wv = w(d._1)
        (acc._1 + wv * d._2, acc._2 + wv)
      })
      p._1 / p._2
    }
  }

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location     Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val distances = temperatures.map(t => (greatCircleDistance(location, t._1), t._2))
    inverseDistanceWeighting(distances)
  }

  def interpolate(xi: Double, xj: Double, fxi: Int, fxj: Int, x: Double): Int =
    math.round(fxi + ((fxj - fxi) / (xj - xi)) * (x - xi)).toInt

  /**
    * @param colors      Pairs containing a value and its associated color
    * @param temperature The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(colors: Iterable[(Temperature, Color)], temperature: Temperature): Color =
    createColorCalculator(colors)(temperature)

  def createColorCalculator(colors: Iterable[(Temperature, Color)]): ColorCalculator = {
    val sortedColorPairs = colors.toIndexedSeq.sortBy(_._1)
    val minp = sortedColorPairs(0)
    val maxp = sortedColorPairs(sortedColorPairs.size - 1)
    val colorsMap = colors.toMap.withDefaultValue(UNKNOWN_COLOR)
    (temperature: Temperature) => {
      if (temperature <= minp._1) {
        minp._2
      } else if (temperature >= maxp._1) {
        maxp._2
      } else {
        val tc = colorsMap(temperature)
        if (tc != UNKNOWN_COLOR) {
          tc
        } else {
          val i = sortedColorPairs.indices.find(idx => sortedColorPairs(idx + 1)._1 > temperature).get
          val ti = sortedColorPairs(i)
          val tj = sortedColorPairs(i + 1)
          val xi = ti._1
          val xj = tj._1
          val red = interpolate(xi, xj, ti._2.red, tj._2.red, temperature)
          val green = interpolate(xi, xj, ti._2.green, tj._2.green, temperature)
          val blue = interpolate(xi, xj, ti._2.blue, tj._2.blue, temperature)
          Color(red, green, blue)
        }
      }
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val tuple2Location = (p: (Int, Int)) => Location(p._1, p._2)
    val temperaturePredictor = predictTemperature(temperatures, _)
    val colorCalculator = createColorCalculator(colors)
    val color2Pixel: Color => Pixel = color => Pixel(color.red, color.green, color.blue, DEFAULT_ALPHA)
    val mapper =
      tuple2Location
        .andThen(temperaturePredictor)
        .andThen(colorCalculator)
        .andThen(color2Pixel)

    val pixels =
      (for {
        lat <- 90 until -90 by -1
        lon <- -180 until 180
      } yield (lat, lon))
        .par
        .map(mapper)
        .toArray

    Image(360, 180, pixels)
  }
}
