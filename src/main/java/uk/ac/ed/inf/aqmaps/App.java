package uk.ac.ed.inf.aqmaps;

import java.util.List;
import com.mapbox.geojson.FeatureCollection;

import java.io.*;

/**
 * Main application which runs the drone simulation 
 */
public class App {
        
    public static void main(String[] args) throws Exception {
    	
    	long startTime = System.nanoTime();
    	    	
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
    	List<NoFlyZoneBuilding> buildings = AppUtils.fetchBuildingCoordinates(portStr);
    	
    	// NOTE: The GeoJSON FeatureCollection Map doesn't need to include the No-Fly-Zone buildings
    	var mapFC = Map.generateMapGeoJson(sensorPoints, buildings);
    	
    	// Instantiate drone's position with given initial position
    	var droneStartingPosition = new Position(startingLongitude, startingLatitude);
    	Drone drone = new Drone(droneStartingPosition, sensorPoints, buildings);
    	
    	// Initialize drone's travel path with starting position
    	drone.addPositionForTravelPath(droneStartingPosition);
    	
    	// Drone flies
    	drone.generateGreedyFlightPath();
    	
    	// Obtain drone travel path from the flight path algorithm
    	var dronePath = drone.getTravelledPath();
    	
    	// Generates final FeatureCollection
    	var finalFC = Map.generateFinalGeoJson(dronePath, FeatureCollection.fromJson(mapFC));
    	
    	System.out.println(finalFC);
    	
    	// For cohesiveness, prints out necessary information for researchers and developers
    	System.out.println("Map Accessed: " + yearStr + "-" + monthStr + "-" + dayStr);
    	System.out.println("Has the drone taken readings for all air-quality sensors on the map?");
    	System.out.println(drone.getNotVisitedSensorPoints().isEmpty());
    	System.out.println("Did the drone return close to its original position?");
    	System.out.println(drone.isReturned());
    	    	
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
    	
    	long endTime = System.nanoTime();
    	long totalTime = endTime - startTime;
    	System.out.println("Execution Time in Seconds: " + totalTime / 1000000000.0 + " seconds");
    }
}
