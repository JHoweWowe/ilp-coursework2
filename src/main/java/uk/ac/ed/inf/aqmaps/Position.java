package uk.ac.ed.inf.aqmaps;

import java.util.List;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;

// Position class which determines longitude and latitude for drones and sensor points in the map
public class Position {
	
	private double longitude;
	private double latitude;
	
	private static final double droneMovementDegrees = 0.0003;
	
	public Position(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	// Check if the drone is within confinement area
	public boolean isWithinConfinementArea() {	    
		if (((getLongitude() > -3.192473) && (getLongitude() < -3.184319)) && 
		((getLatitude() > 55.942617) && (getLatitude() < 55.946233))) {
			return true;
		}
		return false;
	}
	
	// From direction degrees given, calculate drone's new position
	public Position nextPosition(Direction direction) {
		
		// Obtain drone's original position
		double longi = getLongitude();
		double lat = getLatitude();
		
		int degrees = direction.getDirectionInDegrees();
		
		// Math.toRadians corresponds in terms of direction and quadrants
		double diffLongitude = droneMovementDegrees * Math.cos(Math.toRadians(degrees));
		double diffLatitude = droneMovementDegrees * Math.sin(Math.toRadians(degrees));

		longi = longi + diffLongitude;
		lat = lat + diffLatitude;
		
		var newPosition = new Position(longi, lat);
		
		return newPosition;
		
	}
	
	// Getter methods
	public double getLongitude() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	// For debugging purposes
	public static void main(String[] args) {
		//var position = new Position(-3.1878,55.9444);
		//var newPosition = position.nextPosition(new Direction(180));
		//System.out.println(newPosition.getLongitude());
		//System.out.println(newPosition.getLatitude());
	}

}
