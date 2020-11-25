package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

// This class is responsible for generating the GeoJSON map and applying its helper methods

public abstract class Map {
	
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
    		
    		// Otherwise, convert the 
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
    
    // NOTE: There is no need to generate GeoJSON for buildings, but helps to visualize
    public static String generateMapGeoJson(List<SensorPoint> sensorPoints, List<NoFlyZoneBuilding> buildings) {
    	
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
    	
    	for (NoFlyZoneBuilding building : buildings) {
    		var coordinates = building.getCoordinates();
    		var polygon = Polygon.fromLngLats(List.of(coordinates));
    		var feature = Feature.fromGeometry(polygon);
    		features.add(feature);
    	}
    	
    	var featureCollection = FeatureCollection.fromFeatures(features);
    	
        var finalGeoJson = featureCollection.toJson();
        
        return finalGeoJson;

    }

}
