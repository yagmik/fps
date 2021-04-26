package observatory

import math.{Pi, abs, acos, cos, sin}

trait Distances {
  private def isAntipodes(loc1Deg: Location, loc2Deg: Location): Boolean = {
    val lat1Deg = loc1Deg.lat
    val lon1Deg = loc1Deg.lon
    val lat2Deg = loc2Deg.lat
    val lon2Deg = loc2Deg.lon
    lat1Deg == -lat2Deg && (lon1Deg == lon2Deg + 180 || lon1Deg == lon2Deg - 180)
  }

  def greatCircleDistance(loc1Deg: Location, loc2Deg: Location): Distance = {
    if (loc1Deg == loc2Deg) {
      0
    } else if (isAntipodes(loc1Deg, loc2Deg)) {
      Pi * EARTH_RADIUS
    } else {
      val loc1Rad = loc1Deg.toRadians
      val loc2Rad = loc2Deg.toRadians
      val (lat1Rad, lon1Rad, lat2Rad, lon2Rad) = (loc1Rad.lat, loc1Rad.lon, loc2Rad.lat, loc2Rad.lon)
      val deltaLambdaRad = abs(lon1Rad - lon2Rad)
      acos(sin(lat1Rad) * sin(lat2Rad) + cos(lat1Rad) * cos(lat2Rad) * cos(deltaLambdaRad)) * EARTH_RADIUS
    }
  }
}

object Distances extends Distances
