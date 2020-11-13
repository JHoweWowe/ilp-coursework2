package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.io.*;

/**
 * This is the main application
 * Helper function should certainly be called, and are usually from other classes
 *
 */
public class App {
    
    //TODO: From the drone's given travel path, and the featureCollection from Map,
    //create a new GeoJson String
    public static String generateDronePathGeoJSON(List<Position> path, FeatureCollection fc) {
    	// Obtain existing features from FeatureCollection Map
    	var features = fc.features();
    	
    	List<Point> points = new ArrayList<Point>();
    	for (int i = 0; i < path.size(); i++) {
    		var longitude = path.get(i).getLongitude();
    		var latitude = path.get(i).getLatitude();
    		var point = Point.fromLngLat(longitude, latitude);
    		points.add(point);
    	}
    	
    	// From the List of Points, create a LineString for the drone algorithm
    	var lineString = LineString.fromLngLats(points);
    	var lineStringFeature = Feature.fromGeometry(lineString);
    	features.add(lineStringFeature);
    	
    	var featureCollection = FeatureCollection.fromFeatures(features);
    	
    	return featureCollection.toJson();
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
    	var startingPosition = new Position(startingLongitude, startingLatitude);
    	Drone drone = new Drone(startingPosition, points, buildings);
    	// Initialize drone's travel path with starting position
    	drone.addPositionForTravelPath(startingPosition);
    	
    	// Measure distance from drone to any arbitrary SensorPoint- for example: first one which isn't close enough
    	//System.out.println(points.get(21).getLocation());
    	//System.out.println(drone.calculateDistance(drone.getPosition(), points.get(21)));
    	//System.out.println(points.get(21).getLocation());
    	
    	//System.out.println(drone.findClosestNotVisitedSensorPoint(startingPosition).getLocation());
    	    	
    	// Test for 1st point
    	//drone.setNextSensorPoint(points.get(21));
    	//drone.move();
    	
    	//System.out.println(points.get(4).getLocation());
    	//System.out.println(drone.calculateDistance(drone.getPosition(), points.get(4)));
    	//System.out.println(points.get(4).getLocation());
    	
    	// Test for 2nd point
    	//drone.setNextSensorPoint(points.get(4));
    	//drone.move();
    	
    	var mapFC = Map.generateMapGeoJson(points, buildings);
    	drone.generateGreedyFlightPath();
    	var dronePath = drone.getTravelledPath();
    	var finalFC = generateDronePathGeoJSON(dronePath, FeatureCollection.fromJson(mapFC));
    	
    	System.out.println(finalFC);
    	    	
    	//System.out.println(Map.generateMapGeoJson(points,buildings));
    	
    	/** Write to stream according to coursework section 2.4 **/
    	
    	// After printing out the final collection in JSON...create the flight path files
    	String flightPathTextFile = "flightpath-" + dayStr + "-" + monthStr + "-" + yearStr + ".txt";
    	PrintWriter output = new PrintWriter(flightPathTextFile);
    	for (int i = 0; i < drone.getMovements().size(); i++) {
    		output.println(drone.getMovements().get(i));
    	}
    	output.close();
    	
    	// Create geojson readings for particular day, month, year
    	String mapGeoJsonReadingsTextFile = "readings-" + dayStr + "-" + monthStr + "-" + yearStr + ".geojson";
    	PrintWriter output2 = new PrintWriter(mapGeoJsonReadingsTextFile);
    	output2.println(finalFC);
    	output2.close();
    	    	
    }
}
