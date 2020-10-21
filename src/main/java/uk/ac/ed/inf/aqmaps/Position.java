package uk.ac.ed.inf.aqmaps;

/** TODO: In future, implement Position class in SensorPoint and generation of maps **/
public class Position {
	
	private double longitude;
	private double latitude;
		
	
	public Position(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	// From direction degrees given, calculate drone's new position
	
	public Position predictPosition(Drone drone, Direction direction) {
		Position startingPosition = drone.getPosition();
		int degrees = direction.getDirectionInDegrees();
		
		final double droneMovementDegrees = 0.0003;
		
		double a = 10;
		double b = Math.toRadians(a);
		
		double differenceY = droneMovementDegrees * Math.sin(b);
		double differenceX = droneMovementDegrees * Math.cos(b);
		
		
		return startingPosition;
	}
	
	// For debugging purposes
	public static void main(String[] args) {
		System.out.println(0.0003 * Math.sin(Math.toRadians(90)));
		System.out.println(0.0003 * Math.cos(Math.toRadians(90)));
		
		System.out.println(0.0003 * Math.sin(Math.toRadians(10)));
		System.out.println(0.0003 * Math.cos(Math.toRadians(10)));
	}

}
