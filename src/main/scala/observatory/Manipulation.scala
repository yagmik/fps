package observatory

/**
  * 4th milestone: value-added information
  */
object Manipulation extends ManipulationInterface {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val memo = Array.fill(360 * 180)(Double.NaN)

    def gridLocation2Index(gridLocation: GridLocation): Int = {
      val y = 90 - gridLocation.lat
      val x = 180 + gridLocation.lon
      y * 360 + x
    }

    val f: GridLocation => Temperature = (gridLocation: GridLocation) => {
      val idx = gridLocation2Index(gridLocation)
      if (memo(idx).isNaN) {
        memo(idx) = Visualization.predictTemperature(temperatures, gridLocation.toLocation)
        memo(idx)
      } else {
        memo(idx)
      }
    }
    f
  }

  /**
    * @param temperatures Sequence of known temperatures over the years (each element of the collection
    *                     is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val fs =
      temperatures
        .par
        .map(makeGrid)
    val f: GridLocation => Temperature = (gridLocation: GridLocation) => {
      val (sum, cnt) = fs
        .aggregate((0d, 0))(
          (acc, f) => (acc._1 + f(gridLocation), acc._2 + 1),
          (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
        )
      sum / cnt
    }
    f
  }

  /**
    * @param temperatures Known temperatures
    * @param normals      A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val glTemperaturePredictor = makeGrid(temperatures)
    val f: GridLocation => Temperature = (gridLocation: GridLocation) =>
      glTemperaturePredictor(gridLocation) - normals(gridLocation)
    f
  }
}
