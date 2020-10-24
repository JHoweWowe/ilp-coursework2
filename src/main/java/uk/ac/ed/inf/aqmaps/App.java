package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * This is the main application
 * Helper function should certainly be called, and are usually from other classes
 *
 */
public class App {
	
    private static final Point gridPointNW = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point gridPointNE = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point gridPointSW = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point gridPointSE = Point.fromLngLat(-3.184319, 55.942617);
    
    //TODO: From the drone's given travel path, and the featureCollection from Map,
    //create a new GeoJson String
    public static String generateDronePathGeoJSON(List<Position> path, FeatureCollection fc) {
    	return "";
    }
    
    public static void main(String[] args) throws Exception {
    	    	
    	// Checks if arguments length run in command-line are not equal to 7...
    	if (args.length != 7) {
    		throw new IllegalArgumentException("Please input the correct number of arguments");
    	}
    	
    	String dayStr = args[0];
    	String monthStr = args[1];
    	String yearStr = args[2];
    	
    	double startingLatitude = Double.parseDouble(args[3]);
    	double startingLongitude = Double.parseDouble(args[4]);
    	
    	String randomNumberSeedStr = args[5];
    	String portStr = args[6];
    	
    	// Obtains the points / buildings from Utility class
    	List<SensorPoint> points = AppUtils.fetchSensorPointData(dayStr, monthStr, yearStr, portStr);
    	List<NoFlyZoneBuilding> buildings = AppUtils.fetchBuildingCoordinates();
    	
    	// Instantiate drone's position
    	Position startingPosition = new Position(startingLongitude, startingLatitude);
    	Drone drone = new Drone(startingPosition, points);
    	
    	// Measure distance from drone to any arbitrary SensorPoint- for example: first one which isn't close enough
    	System.out.println(points.get(21).getLocation());
    	System.out.println(drone.calculateDistance(points.get(21)));
    	
    	System.out.println(points.get(21).getLocation());
    	
    	drone.setNextSensorPoint(points.get(21));
    	drone.move();
    	    	
    	
    	System.out.println(Map.generateMapGeoJson(points,buildings));
    	
    }
}
