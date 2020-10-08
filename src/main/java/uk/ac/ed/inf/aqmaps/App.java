package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

/**
 * This is the main application
 *
 */
public class App {
	
    private static final Point gridPointNW = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point gridPointNE = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point gridPointSW = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point gridPointSE = Point.fromLngLat(-3.184319, 55.942617);
    
    // Hidden function
    public static void createPoints() {
    	
        double[] coordinates = AppUtils.coordinatesFromLocation("test");
        Point testPoint = Point.fromLngLat(coordinates[0], coordinates[1]);
        
        var featureTwo = Feature.fromGeometry(testPoint);
    	
        // Ultimate Goal- Create Feature Collection JSON format to generate the whole application
    	
    	// First let's create the confinement area as a LineString
        // List of Coordinates must be added in following order, follows right-hand rule 
        var gridPoints = new ArrayList<Point>();
        gridPoints.add(gridPointNW);
        gridPoints.add(gridPointNE);
        gridPoints.add(gridPointSE);
        gridPoints.add(gridPointSW);
        gridPoints.add(gridPointNW);
                        
        // Create the map's boundaries 
        var mapBoundary = LineString.fromLngLats(gridPoints);
        var feature = Feature.fromGeometry(mapBoundary);
            	
    	var features = new ArrayList<Feature>();
    	features.add(feature);
    	
    	features.add(featureTwo);
    	
        var featureCollection = FeatureCollection.fromFeatures(features);
        
        // Ultimate Goal- generate a JSON map
        System.out.println(featureCollection.toJson());
    }
	
    public static void main( String[] args ) throws Exception {
    	
    	// First assume my project is run with command-line arguments
    	
    	// Checks if arguments length run in command-line are not equal to 7...
    	if (args.length != 7) {
    		throw new IllegalArgumentException("Please input the right arguments");
    	}
    	
    	String dayStr = args[0];
    	String monthStr = args[1];
    	String yearStr = args[2];
    	
    	String latitudeStr = args[3];
    	String longitudeStr = args[4];
    	
    	String randomNumberSeedStr = args[5];
    	String portStr = args[6];
    	
    	// Test LOL- THAT IS THE FUCKING HARDEST SHIT I HAVE DONE
    	List<SensorPoint> points = AppUtils.fetchSensorPointData(dayStr, monthStr, yearStr, portStr);
    	for (SensorPoint point : points) {
    		System.out.println(point.getLocation());
    	}
    	
    	
    }
}
