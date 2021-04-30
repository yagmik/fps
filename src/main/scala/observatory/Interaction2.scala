package observatory

import observatory.LayerName._

/**
  * 6th (and last) milestone: user interface polishing
  */
object Interaction2 extends Interaction2Interface {

  /**
    * @return The available layers of the application
    */
  def availableLayers: Seq[Layer] = {
    val temperatures = Temperatures
    val deviations = Deviations
    val temperaturesLayer = Layer(temperatures, temperatures.colorScale, temperatures.yearRange)
    val deviationsLayer = Layer(deviations, deviations.colorScale, deviations.yearRange)
    Seq(temperaturesLayer, deviationsLayer)
  }

  /**
    * @param selectedLayer A signal carrying the layer selected by the user
    * @return A signal containing the year bounds corresponding to the selected layer
    */
  def yearBounds(selectedLayer: Signal[Layer]): Signal[Range] = {
    Signal(selectedLayer().bounds)
  }

  /**
    * @param selectedLayer The selected layer
    * @param sliderValue   The value of the year slider
    * @return The value of the selected year, so that it never goes out of the layer bounds.
    *         If the value of `sliderValue` is out of the `selectedLayer` bounds,
    *         this method should return the closest value that is included
    *         in the `selectedLayer` bounds.
    */
  def yearSelection(selectedLayer: Signal[Layer], sliderValue: Signal[Year]): Signal[Year] = {
    val bounds = Signal(selectedLayer().bounds)
    val minYear = Signal(bounds()(0))
    val maxYear = Signal(bounds()(bounds().length - 1))
    Signal(math.min(math.max(minYear(), sliderValue()), maxYear()))
  }

  /**
    * @param selectedLayer The selected layer
    * @param selectedYear  The selected year
    * @return The URL pattern to retrieve tiles
    */
  def layerUrlPattern(selectedLayer: Signal[Layer], selectedYear: Signal[Year]): Signal[String] = {
    // TODO replace to string interpolation
    Signal("target/" + selectedLayer().layerName.id + "/" + selectedYear() + "/{z}/{x}-{y}.png")
  }

  /**
    * @param selectedLayer The selected layer
    * @param selectedYear  The selected year
    * @return The caption to show
    */
  def caption(selectedLayer: Signal[Layer], selectedYear: Signal[Year]): Signal[String] = {
    val name = Signal(selectedLayer().layerName.id)
    Signal(name().substring(0, 1).toUpperCase + name().substring(1) + " (" + selectedYear() + ")")
  }

}

// Interface used by the grading infrastructure. Do not change signatures
// or your submission will fail with a NoSuchMethodError.
trait Interaction2Interface {
  def availableLayers: Seq[Layer]

  def yearBounds(selectedLayer: Signal[Layer]): Signal[Range]

  def yearSelection(selectedLayer: Signal[Layer], sliderValue: Signal[Year]): Signal[Year]

  def layerUrlPattern(selectedLayer: Signal[Layer], selectedYear: Signal[Year]): Signal[String]

  def caption(selectedLayer: Signal[Layer], selectedYear: Signal[Year]): Signal[String]
}

sealed abstract class LayerName(val id: String)

object LayerName {
  case object Temperatures extends LayerName("temperatures") {
    val colorScale = colors
    val yearRange = 1975 to 2015
  }

  case object Deviations extends LayerName("deviations") {
    val colorScale = Seq(
      (-7d, Color(0, 0, 255)),
      (-2d, Color(0, 255, 255)),
      (0d, Color(255, 255, 255)),
      (2d, Color(255, 255, 0)),
      (4d, Color(255, 0, 0)),
      (7d, Color(0, 0, 0))
    )
    val yearRange: Range = 1990 to 2015
  }
}

/**
  * @param layerName  Name of the layer
  * @param colorScale Color scale used by the layer
  * @param bounds     Minimum and maximum year supported by the layer
  */
case class Layer(layerName: LayerName, colorScale: Seq[(Temperature, Color)], bounds: Range)
