package uk.ac.ed.inf.aqmaps;

import java.util.List;
import java.util.ArrayList;

public class Drone {
	
	private Position position;
	
	// Next SensorPoint
	private SensorPoint nextSensorPoint;
	
	private List<Position> travelledPath;
	
	private List<SensorPoint> visited;
	private List<SensorPoint> notVisited;
	
	private final double droneMovementDegrees = 0.0003;
	private int numberOfMoves = 150;
	
	// When the drone is instantiated, drone should not have visited any points so far
	public Drone(Position position, List<SensorPoint> points) {
		this.position = position;
		// Drone's visited Sensorpoints should be empty but notVisited should be the points given
		visited = new ArrayList<SensorPoint>();
		this.notVisited = points;
		// Drone's initial travel path should be completely empty
		this.travelledPath = new ArrayList<Position>();
	}
	
	/** GETTER METHODS **/
	// Obtain drone position
	public Position getPosition() {
		return position;
	}
	
	// Set new position
	public void setPosition(Position newPosition) {
		this.position = newPosition;
	}
	
	public SensorPoint getNextSensorPoint() {
		return nextSensorPoint;
	}
	
	public void setNextSensorPoint(SensorPoint nextSensorPoint) {
		this.nextSensorPoint = nextSensorPoint;
	}
	
	public List<SensorPoint> getVisitedSensorPoints() {
		return visited;
	}
	
	public void addVisitedSensorPoint() {
		visited.add(nextSensorPoint);
	}
	
	public List<Position> getTravelledPath() {
		return travelledPath;
	}
	
	public void addPositionForTravelPath(Position newPosition) {
		travelledPath.add(newPosition);
	}
	
	// Main method for flight path??? Should it be under Drone class??? Can be iterated and improved
	// THIS is for a greedy algorithm- most likely will have to improve this algorithm
	// Has to search through 33! possible paths...
	public void generateFlightPath() {}
	
	// Test for one flight path move- PLEASE DON'T USE IT YET
	public void generateFlightPathTest() {
		
		//NOTE: There should be a while loop enclosing this algorithm
		//while the number of moves > 0
		
		// Check for the closest point for the not-visited SensorPoints
		if (notVisited.size() > 0) {
			
			double minDistance = 9999.99;
			int minDistanceIdx = -1;
			
			// Check through all the non-visited points
			for (int i = 0; i < notVisited.size(); i++) {
				
				if (minDistance < calculateDistance(notVisited.get(i))) {
					
					minDistance = calculateDistance(notVisited.get(i));
					minDistanceIdx = i;
					
				}
				
			}
			
			visited.add(notVisited.get(minDistanceIdx));
			
		}

	}
	
	/** Drone methods- includes movement and take reading of sensor point **/
	// DETERMINE in which direction should the drone fly in FOR each move AFTER knowing which sensor point to go to
	public void move() {
		System.out.println(getPosition().getLongitude());
		System.out.println(getPosition().getLatitude());
		// Checks for each viable direction
		double minDistance = 99999.99;
		int bestDirectionAngle = 0;
		for (int directionAngle = 0; directionAngle < 360; directionAngle += 10) {
			// Check drone position
			var dronePosition = position.nextPosition(new Direction(directionAngle));
			setPosition(dronePosition);
			double distance = calculateDistance(nextSensorPoint);
			if (distance < minDistance) {
				minDistance = distance;
				bestDirectionAngle = directionAngle;
			}
		}
		var newPosition = position.nextPosition(new Direction(bestDirectionAngle));
		addPositionForTravelPath(newPosition);
		// Set the drone's new location
		setPosition(newPosition);
		
		// Debugging purposes only
		System.out.println(getPosition().getLongitude());
		System.out.println(getPosition().getLatitude());
		
		System.out.println("Best Direction Angle: " + bestDirectionAngle);
		System.out.println("Min Distance: " + minDistance);
	}
	//TODO: ONLY should add the SensorPoint if it is within 0.0002,
	// and then you can add respective SensorPoint as visited
	
	// DOUBLE CHECK PLEASE
	public void takeReading() {
		double distance = calculateDistance(nextSensorPoint);
		if (distance < 0.0002) {
			visited.add(nextSensorPoint);
		}
	}
	
	// Helper function which calculates distance between drone's current position and any arbitrary sensor point position
	public double calculateDistance(SensorPoint point) {
		Position dronePosition = getPosition();
		double x1 = dronePosition.getLongitude();
		double x2 = point.getPosition().getLongitude();
		double y1 = dronePosition.getLatitude();
		double y2 = point.getPosition().getLatitude();
		double a = Math.pow(x1-x2, 2);
		double b = Math.pow(y1-y2, 2);
		return Math.sqrt(a+b);
	}
	
	// For debugging purposes ONLY
	public static void main(String[] args) {

	}
	

}
