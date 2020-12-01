package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

// This class is responsible for generating the GeoJSON map and applying its helper methods
// Also responsible for creating the visuals of the drone flight path and boundaries of NoFlyZone buildings

public class Map {
	
    /**
     * Converts the air quality reading value into a RGB string for color
     * @param readingValueStr - Sensor Point's air quality reading value
     * @return String
     */
    public static String readingValueToRGBString(String readingValueStr) {
    	
    	var rgbString = "";
    	
    	// Check if reading value can be parsed as a double
    	try {
    		// Assumes if the battery level is below 10%, which results in either null or NaN values
    		if ((readingValueStr.equals("null")) || (readingValueStr.equals("NaN"))) {
    			rgbString = "#000000";
    			return rgbString;
    		}
    		
    		// Otherwise, convert the reading value into a double
    		double value = Double.parseDouble(readingValueStr);
    		
            if ((value >= 0) && (value < 32)) {
                rgbString = "#00ff00";
            }
            else if ((value >= 32) && (value < 64)) {
                rgbString = "#40ff00";
            }
            else if ((value >= 64) && (value < 96)) {
                rgbString = "#80ff00";
            }
            else if ((value >= 96) && (value < 128)) {
                rgbString = "#c0ff00";
            }
            else if ((value >= 128) && (value < 160)) {
                rgbString = "#ffc000";
            }
            else if ((value >= 160) && (value < 192)) {
                rgbString = "#ff8000";
            }
            else if ((value >= 192) && (value < 224)) {
                rgbString = "#ff4000";
            }
            else if ((value >= 224) && (value < 256)) {
                rgbString = "#ff0000";
            }
    	}
    	catch (NumberFormatException e) {
    		System.err.println("Invalid number format!");
    	}
    	// Assume if number is negative or greater than 255, then return nothing
    	return rgbString;
    }
	
    public static String readingValueMarkerSymbol(String readingValueStr) {
    	
    	var markerSymbol = "";
    	
    	try {
    		if ((readingValueStr.equals("null")) || (readingValueStr.equals("NaN"))) {
    			markerSymbol = "cross";
    			return markerSymbol;
    		}
    		double value = Double.parseDouble(readingValueStr);
    		if ((value >= 0) && (value < 128)) {
    			markerSymbol = "lighthouse";
    		}
    		else if ((value >= 128) && value < 256) {
    			markerSymbol = "danger";
    		}
    	}
    	catch (NumberFormatException e) {
    		markerSymbol = "";
    	}
    	
    	return markerSymbol;
    }
    
    public static String generateMapGeoJson(List<SensorPoint> sensorPoints) {
    	
    	var features = new ArrayList<Feature>();
    	
    	for (SensorPoint sensorPoint : sensorPoints) {
    		
    		// For debugging
    		//System.out.println(sensorPoint.getLocation());
    		//System.out.println(sensorPoint.getPosition().getLongitude() + " " + sensorPoint.getPosition().getLatitude());
    		//System.out.println(readingValueToRGBString(sensorPoint.getSensorReading()));
    		
    		Point p = Point.fromLngLat(sensorPoint.getPosition().getLongitude(), sensorPoint.getPosition().getLatitude());
    		var feature = Feature.fromGeometry(p);
    		feature.addStringProperty("location", sensorPoint.getLocation());
    		feature.addStringProperty("rgb-string", readingValueToRGBString(sensorPoint.getSensorReading()));
            feature.addStringProperty("marker-color", readingValueToRGBString(sensorPoint.getSensorReading()));
            feature.addStringProperty("marker-symbol", readingValueMarkerSymbol(sensorPoint.getSensorReading()));
    		
    		features.add(feature);
    	}
    	
    	var featureCollection = FeatureCollection.fromFeatures(features);
    	
        var finalGeoJson = featureCollection.toJson();
        
        return finalGeoJson;

    }

	// This would be useful for checking if the drone's anticipated path would intersect any buildings
	// Assume number of buildings is 4	
	
	public static Line2D.Double createLine2D(Position p1, Position p2) {
		var point1 = new Point2D.Double(p1.getLongitude(), p1.getLatitude());
		var point2 = new Point2D.Double(p2.getLongitude(), p2.getLatitude());
		var line = new Line2D.Double(point1, point2);
		return line;
	}
	
	public static Path2D.Double createPath2D(NoFlyZoneBuilding building) {
		var path = new Path2D.Double();
		var coordinates = building.getCoordinates();
		path.moveTo(coordinates.get(0).longitude(), coordinates.get(0).latitude());
		for (int i = 1; i < coordinates.size(); i++) {
			path.lineTo(coordinates.get(i).longitude(), coordinates.get(i).latitude());
		}
		path.closePath();
		return path;
	}
	
	// Algorithm implemented with following pseudocode from StackOverFlow- reference included in report
	public static boolean intersects(Path2D.Double path, Line2D line) {
		Point2D.Double start = null;
		Point2D.Double point1 = null;
		Point2D.Double point2 = null;
		for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
			double[] coordinates = new double[6];
		    switch (pi.currentSegment(coordinates)) {
		    case PathIterator.SEG_MOVETO:
		      point2 = new Point2D.Double(coordinates[0], coordinates[1]);
		      point1 = null;
		      start = (Point2D.Double) point2.clone();
		      break;
		    case PathIterator.SEG_LINETO:
		      point1 = point2;
		      point2 = new Point2D.Double(coordinates[0], coordinates[1]);
		      break;
		    case PathIterator.SEG_CLOSE:
		      point1 = point2;
		      point2 = start;
		      break;
		    }
		    if (point1 != null) {
		      Line2D segment = new Line2D.Double(point1, point2);
		      if (segment.intersectsLine(line))
		        return true;
		    }
		  }
		return false;
	}
	
    // Creates a GeoJSON String which generates drone's flight path from the drone's designated travel path and
	// FeatureCollection maps
    public static String generateFinalGeoJson(List<Position> path, FeatureCollection fc) {
    	
    	// Obtain existing features from map details as a FeatureCollection
    	var features = fc.features();
    	
    	// Obtain drone flight path and implement it as a LineString and add it as a FeatureCollection
    	var points = new ArrayList<Point>();
    	for (int i = 0; i < path.size(); i++) {
    		var longitude = path.get(i).getLongitude();
    		var latitude = path.get(i).getLatitude();
    		var point = Point.fromLngLat(longitude, latitude);
    		points.add(point);
    	}
    	// From list of points, create the complete LineString for the drone algorithm
    	var lineString = LineString.fromLngLats(points);
    	var droneFlightPathFeature = Feature.fromGeometry(lineString);
    	
    	// Add drone flight path feature to the list of features
    	features.add(droneFlightPathFeature);
    	
    	// Return the GeoJSON output
    	var featureCollection = FeatureCollection.fromFeatures(features);
    	
    	return featureCollection.toJson();
    }

 
}
