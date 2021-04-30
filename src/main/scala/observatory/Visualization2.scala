package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 extends Visualization2Interface {

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00   Top-left value
    * @param d01   Bottom-left value
    * @param d10   Top-right value
    * @param d11   Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(point: CellPoint,
                            d00: Temperature, d01: Temperature,
                            d10: Temperature, d11: Temperature): Temperature = {
    val x = point.x
    val y = point.y
    d00 * (1 - x) * (1 - y) + d01 * (1 - x) * y + d10 * x * (1 - y) + d11 * x * y
  }

  /**
    * @param grid   Grid to visualize
    * @param colors Color scale to use
    * @param tile   Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(grid: GridLocation => Temperature,
                    colors: Iterable[(Temperature, Color)],
                    tile: Tile
                   ): Image = {
    val colorCalculator = Visualization.createColorCalculator(colors)
    val color2Pixel: Color => Pixel = color => Pixel(color.red, color.green, color.blue, DEFAULT_ALPHA)
    val pixels = tile
      .generateSubTiles(8)
      .par
      .map(p => getTemperature(p.toLocation, grid))
      .map(colorCalculator)
      .map(color2Pixel)
      .toArray
    Image(256, 256, pixels)
  }

  def getTemperature(location: Location, grid: GridLocation => Temperature): Temperature = {
    val d00: Temperature = getD00(location, grid)
    val d10: Temperature = getD10(location, grid)
    val d01: Temperature = getD01(location, grid)
    val d11: Temperature = getD11(location, grid)

    val point = getCellPoint(location)
    bilinearInterpolation(point, d00, d01, d10, d11)
  }

  def getD00(location: Location, grid: GridLocation => Temperature): Temperature = {
    val lat: Int = {
      val l = math.floor(location.lat).toInt
      if (l < -89) 90 else l
    }
    val lon: Int = {
      val l = math.floor(location.lon).toInt
      if (l < -180) 179 else l
    }
    grid(GridLocation(lat, lon))
  }

  def getD10(location: Location, grid: GridLocation => Temperature): Temperature = {
    val lat: Int = {
      val l = math.floor(location.lat).toInt
      if (l < -89) 90 else l
    }
    val lon: Int = {
      val l = math.ceil(location.lon).toInt
      if (l > 179) -180 else l
    }
    grid(GridLocation(lat, lon))
  }

  def getD01(location: Location, grid: GridLocation => Temperature): Temperature = {
    val lat: Int = {
      val l = math.ceil(location.lat).toInt
      if (l > 90) -89 else l
    }
    val lon: Int = {
      val l = math.floor(location.lon).toInt
      if (l < -180) 179 else l
    }
    grid(GridLocation(lat, lon))
  }

  def getD11(location: Location, grid: GridLocation => Temperature): Temperature = {
    val lat: Int = {
      val l = math.ceil(location.lat).toInt
      if (l > 90) -89 else l
    }
    val lon: Int = {
      val l = math.ceil(location.lon).toInt
      if (l > 179) -180 else l
    }
    grid(GridLocation(lat, lon))
  }

  def getCellPoint(location: Location): CellPoint = {
    val flLat = math.floor(location.lat)
    val flLon = math.floor(location.lon)

    val y = location.lat - flLat
    val x = location.lon - flLon

    CellPoint(x, y)
  }
}
