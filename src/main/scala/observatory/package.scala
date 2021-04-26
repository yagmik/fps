package object observatory {
  type Temperature = Double // °C, introduced in Week 1
  type Year = Int // Calendar year, introduced in Week 1
  val colors: Iterable[(Temperature, Color)] = Map(
    60d -> Color(255, 255, 255),
    32d -> Color(255, 0, 0),
    12d -> Color(255, 255, 0),
    0d -> Color(0, 255, 255),
    -15d -> Color(0, 0, 255),
    -27d -> Color(255, 0, 255),
    -50d -> Color(33, 0, 107),
    -60d -> Color(0, 0, 0)
  ).toSeq
  val DEFAULT_ALPHA = 127
  val EARTH_RADIUS = 6371d
  val DEFAULT_POWER_PARAMETER = 6
  type Distance = Double
  val UNKNOWN_COLOR: Color = Color(-1, -1, -1)
  type ColorCalculator = Temperature => Color
}
