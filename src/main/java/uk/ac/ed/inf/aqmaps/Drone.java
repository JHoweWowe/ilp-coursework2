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
		
	// MAIN METHOD: Flight Path- Greedy Algorithm [NOT EFFICIENT- WILL NEED TO IMPROVE IT]
	public void generateGreedyFlightPath() {
		while (numberOfMoves > 0) {
			if (!(notVisited.isEmpty())) {
				// Searches to find NOT VISITED closest SensorPoint based from drone position and then set it
				var dronePosition = getPosition();
				var sensorPoint = findClosestNotVisitedSensorPoint(dronePosition);
				setNextSensorPoint(sensorPoint);
				if (!(isVisited(sensorPoint))) {
					move();
					takeReading(dronePosition);
					numberOfMoves--;
				}
			}
			if (notVisited.isEmpty()) {
				System.out.println("Number of Moves Remaining: " + numberOfMoves);
				break;
			}
		}
	}
	
	// Helper function: find closest not Visited SensorPoint
	public SensorPoint findClosestNotVisitedSensorPoint(Position dronePosition) {
		
		var notVisitedSensorPoints = getNotVisitedSensorPoints();
		double minDistance = Integer.MAX_VALUE;
		SensorPoint closestSensorPoint = notVisitedSensorPoints.get(0);
		// Search for other sensorPoints if applicable
		for (int i = 1; i < notVisitedSensorPoints.size(); i++) {
			var sensorPoint = notVisitedSensorPoints.get(i);
			var distance = calculateDistance(dronePosition, sensorPoint);
			if (distance < minDistance) {
				minDistance = distance;
				closestSensorPoint = sensorPoint;
			}
		}
		return closestSensorPoint;
	}
	
	
	/** Drone methods- includes movement and take reading of sensor point **/
	// Searches and determines which direction should the drone fly in 
	// FOR each move AFTER knowing which sensor point to go to
	public void move() {
		System.out.println(getPosition().getLongitude());
		System.out.println(getPosition().getLatitude());
		// Checks for each viable direction
		double minDistance = Integer.MAX_VALUE;
		int bestDirectionAngle = 0;
		for (int directionAngle = 0; directionAngle < 360; directionAngle += 10) {
			// Check possible drone position
			var droneCurrentPosition = getPosition();
			var droneNextPosition = droneCurrentPosition.nextPosition(new Direction(directionAngle));
			var nextSensorPoint = getNextSensorPoint();
			double distance = calculateDistance(droneNextPosition, nextSensorPoint);
			// TODO: Also check the boundaries for movement..it ultimately determines whether
			// it can be assigned
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
	
	public boolean isVisited(SensorPoint point) {
		// Checks if SensorPoint is read
		var visited = getVisitedSensorPoints();
		if (visited.contains(point)) {
			return true;
		}
		return false;
	}
	
	//TODO: ONLY should add the SensorPoint if the drone's position is within 0.0002,
	// and then you can add respective SensorPoint as visited and remove it from notVisited
	public void takeReading(Position dronePosition) {
		double distance = calculateDistance(dronePosition, nextSensorPoint);
		if (distance < 0.0002) {
			visited.add(nextSensorPoint);
			notVisited.remove(nextSensorPoint);
		}
	}
	
	// Helper function which calculates distance with respect to the drone's current position 
	// and any arbitrary sensor point position
	public double calculateDistance(Position dronePosition, SensorPoint point) {
		double x1 = dronePosition.getLongitude();
		double x2 = point.getPosition().getLongitude();
		double y1 = dronePosition.getLatitude();
		double y2 = point.getPosition().getLatitude();
		double a = Math.pow(x1-x2, 2);
		double b = Math.pow(y1-y2, 2);
		return Math.sqrt(a+b);
	}
	
	/** GETTER METHODS **/
	// Obtain drone position
	public Position getPosition() {
		return position;
	}
	public SensorPoint getNextSensorPoint() {
		return nextSensorPoint;
	}
	public List<SensorPoint> getVisitedSensorPoints() {
		return visited;
	}
	public List<SensorPoint> getNotVisitedSensorPoints() {
		return notVisited;
	}
	
	public List<Position> getTravelledPath() {
		return travelledPath;
	}
	
	/** SETTER METHODS **/
	// Set new position
	public void setPosition(Position newPosition) {
		this.position = newPosition;
	}
	public void setNextSensorPoint(SensorPoint nextSensorPoint) {
		this.nextSensorPoint = nextSensorPoint;
	}
	
	public void addVisitedSensorPoint() {
		visited.add(nextSensorPoint);
	}
	public void addPositionForTravelPath(Position newPosition) {
		travelledPath.add(newPosition);
	}
	
	// For debugging purposes ONLY
	public static void main(String[] args) {

	}
	

}
