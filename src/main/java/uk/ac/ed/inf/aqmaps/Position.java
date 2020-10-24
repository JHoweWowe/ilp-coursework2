package uk.ac.ed.inf.aqmaps;

// Position class which determines longitude and latitude for drones and sensor points in the map
public class Position {
	
	private double longitude;
	private double latitude;
	
	private static final double droneMovementDegrees = 0.0003;
	
	public Position(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	/** Getter and setter methods **/
	public double getLongitude() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
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
	
	// For debugging purposes
	public static void main(String[] args) {
		
		Position position = new Position(-3.1878,55.9444);
		Position newPosition = position.nextPosition(new Direction(180));
		System.out.println(newPosition.getLongitude());
		System.out.println(newPosition.getLatitude());
		
	}

}
