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
 * Main application which generates GeoJSON and flight path text files based from command line arguments
 *
 */
public class App {
    
    // Creates a GeoJSON String which generates drone's flight path from the drone's designated travel path and
	// FeatureCollection maps
    public static String generateDronePathGeoJSON(List<Position> path, FeatureCollection fc) {
    	
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
    	// From list of points, create a LineString for the drone algorithm
    	var lineString = LineString.fromLngLats(points);
    	var droneFlightPathFeature = Feature.fromGeometry(lineString);
    	
    	// Add drone flight path to the list of features
    	features.add(droneFlightPathFeature);
    	
    	// Return the GeoJSON output
    	var featureCollection = FeatureCollection.fromFeatures(features);
    	
    	return featureCollection.toJson();
    }
    
    public static void main(String[] args) throws Exception {
    	    	
    	if (args.length != 7) {
    		throw new IllegalArgumentException("Please input the correct number of arguments");
    	}
    	
    	// Assume command line arguments are in the form of a String
    	var dayStr = args[0];
    	var monthStr = args[1];
    	var yearStr = args[2];
    	var startingLatitude = Double.parseDouble(args[3]);
    	var startingLongitude = Double.parseDouble(args[4]);
    	var randomNumberSeedStr = args[5];
    	var portStr = args[6];
    	
    	// Obtains the points / buildings from AppUtils class
    	List<SensorPoint> sensorPoints = AppUtils.fetchSensorPointData(dayStr, monthStr, yearStr, portStr);
    	List<NoFlyZoneBuilding> buildings = AppUtils.fetchBuildingCoordinates();
    	
    	// Instantiate drone's position with initial position
    	var startingPosition = new Position(startingLongitude, startingLatitude);
    	Drone drone = new Drone(startingPosition, sensorPoints, buildings);
    	
    	// Initialize drone's travel path with starting position
    	drone.addPositionForTravelPath(startingPosition);
    	
    	// TODO: The FeatureCollection doesn't need to include the buildings
    	var mapFC = Map.generateMapGeoJson(sensorPoints, buildings);
    	
    	// Drone flies
    	drone.generateGreedyFlightPath();
    	
    	// Obtain drone travel path from the flight path algorithm
    	var dronePath = drone.getTravelledPath();
    	
    	// Generates final FeatureCollection
    	var finalFC = generateDronePathGeoJSON(dronePath, FeatureCollection.fromJson(mapFC));
    	
    	System.out.println(finalFC);
    	
    	/** Write to stream according to coursework section 2.4 **/
    	// Create flight path text files
    	String flightPathTextFile = "flightpath-" + dayStr + "-" + monthStr + "-" + yearStr + ".txt";
    	PrintWriter output = new PrintWriter(flightPathTextFile);
    	for (int i = 0; i < drone.getMovements().size(); i++) {
    		output.println(drone.getMovements().get(i));
    	}
    	output.close();
    	// Create GeoJSON readings for particular day, month, year
    	// Default should be 01-01-2020 for submission purposes
    	String mapGeoJsonReadingsTextFile = "readings-" + dayStr + "-" + monthStr + "-" + yearStr + ".geojson";
    	PrintWriter output2 = new PrintWriter(mapGeoJsonReadingsTextFile);
    	output2.println(finalFC);
    	output2.close();	
    }
}
