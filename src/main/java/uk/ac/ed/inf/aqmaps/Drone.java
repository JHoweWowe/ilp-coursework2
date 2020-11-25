package uk.ac.ed.inf.aqmaps;

import java.util.List;

import com.mapbox.geojson.Point;

import java.util.ArrayList;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;


public class Drone {
	
	private Position position;
	// Next SensorPoint
	private SensorPoint nextSensorPoint;
	private List<Position> travelledPath;
	private List<SensorPoint> visited;
	private List<SensorPoint> notVisited;
	private List<NoFlyZoneBuilding> buildingsToAvoid;
	
	// String text representation for movements- used to generate text files in App.java
	private List<String> movements;
	
	// Boolean indicating if drone should return to original location- only activated after obtaining all sensor points
	private boolean sensoredAllPointsAndNearOriginalLocation;
		
	// One of the attributes required to record every movement
	private int lastBestDirectionAngle;
	
	private int numberOfMoves = 150;
	
	// When the drone is instantiated, drone should not have visited any points so far
	public Drone(Position position, List<SensorPoint> points, List<NoFlyZoneBuilding> buildingsToAvoid) {
		this.position = position;
		// Drone's visited Sensorpoints should be empty but notVisited should be the points given
		visited = new ArrayList<SensorPoint>();
		this.notVisited = points;
		// Drone's initial travel path should be completely empty
		this.travelledPath = new ArrayList<Position>();
		this.buildingsToAvoid = buildingsToAvoid;
		this.movements = new ArrayList<String>();
		
		this.lastBestDirectionAngle = 0;
		this.sensoredAllPointsAndNearOriginalLocation = false;
	}
		
	// MAIN METHOD: Flight Path- Greedy Algorithm [Somewhat efficient]
	public void generateGreedyFlightPath() {
		while (numberOfMoves > 0) {
			if (!(notVisited.isEmpty())) {
				// Searches to find NOT VISITED closest SensorPoint based from drone position and then set it
				var dronePosition = getPosition();
				var sensorPoint = findClosestNotVisitedSensorPoint(dronePosition);
				setNextSensorPoint(sensorPoint);
				if (!(isVisited(sensorPoint))) {
					// Move towards that sensor point which is not visited and take reading if applicable
					move();
					
					var newDronePosition = getPosition();
					var lastBestDirectionAngle = getAngle();

					String pointStr = takeReading(newDronePosition);
					
					// Generates the movement String text- could be moved outside of function
					int moveNumber = getMovements().size()+1;					
					String movement = createStringMovement(moveNumber, dronePosition, lastBestDirectionAngle, 
							newDronePosition, pointStr);
					// Adds to the Movements function
					getMovements().add(movement);
					
					numberOfMoves--;
				}
			}
			if (notVisited.isEmpty() && (!(isReturned()))) {
				var dronePosition = getPosition();
				
				System.out.println("Drone returning towards original location");
				returnTowardsOriginalLocation();
				
				var lastBestDirectionAngle = getAngle();
				var newDronePosition = getPosition();
							
				// Generates the movement String text- could be moved outside of function
				int moveNumber = getMovements().size()+1;					
				String movement = createStringMovement(moveNumber, dronePosition, lastBestDirectionAngle, 
						newDronePosition, "null");
				// Adds to the Movements function
				getMovements().add(movement);
				
				numberOfMoves--;
			}
			if ((notVisited.isEmpty()) && (isReturned())) {
				System.out.println("Number of Moves Remaining: " + numberOfMoves);
				// Terminate algorithm when finished
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
			var sensorPointPosition = sensorPoint.getPosition();
			var distance = calculateDistance(dronePosition, sensorPointPosition);
			if (distance < minDistance) {
				minDistance = distance;
				closestSensorPoint = sensorPoint;
			}
		}
		return closestSensorPoint;
	}
		
	// Checks if drone position is within confinement area and if it intersects any of buildings
	public boolean meetsAllRequiredConstraints(Position droneNextPosition, Line2D.Double line, 
			Path2D.Double building1, Path2D.Double building2, Path2D.Double building3, Path2D.Double building4) {
		var meetsRequirements = false;
		
		// First checks if drone's next anticipated position is within confinement area
		if (droneNextPosition.isWithinConfinementArea()) {
			// Checks if anticipated drone paths intersect any of buildings
			if ((!(Map.intersects(building1, line))) && (!(Map.intersects(building2, line)))) {
				if ((!(Map.intersects(building3, line))) && (!(Map.intersects(building4, line)))) {
					meetsRequirements = true;
				}
			}
		}
		
		return meetsRequirements;
		
	}
	
	/** Drone methods- includes movement and take reading of sensor point **/
	// Searches and determines which direction should the drone fly in 
	// FOR each move AFTER knowing which sensor point to go to
	
	// Returns a String representation of the movement while doing the movement itself
	public void move() {
		// Obtains list of NoFlyZoneBuildings
		var buildings = getNoFlyZoneBuildings();
		
		// Obtains drone current position
		var droneCurrentPosition = getPosition();
		
		// Gets the sensorPoint that the drone should move towards to and take reading if applicable
		var nextSensorPoint = getNextSensorPoint();
		
		// Manually create the boundaries for the buildings using Path2D!
		var building1 = Map.createPath2D(buildings.get(0));
		var building2 = Map.createPath2D(buildings.get(1));
		var building3 = Map.createPath2D(buildings.get(2));
		var building4 = Map.createPath2D(buildings.get(3));
		
		// Checks for each viable direction
		double minDistance = Integer.MAX_VALUE;
		int bestDirectionAngle = 0;
		for (int directionAngle = 0; directionAngle < 360; directionAngle += 10) {
			
			// Check possible drone position
			var droneNextPosition = droneCurrentPosition.nextPosition(new Direction(directionAngle));
			double distance = calculateDistance(droneNextPosition, nextSensorPoint.getPosition());
			
			var lineStr = Map.createLine2D(droneCurrentPosition, droneNextPosition);
												
			if (meetsAllRequiredConstraints(droneNextPosition, lineStr, building1, building2, building3, building4)) {
				// Finally check for the minimal distance
				if (distance < minDistance) {
					minDistance = distance;
					bestDirectionAngle = directionAngle;
				}
			}

		}
		var newPosition = position.nextPosition(new Direction(bestDirectionAngle));
		addPositionForTravelPath(newPosition);
		// Set the drone's new location
		setPosition(newPosition);
		
		// Sets previous best direction angle
		setAngle(bestDirectionAngle);
				
		// Debugging purposes only
		System.out.println(getPosition().getLongitude());
		System.out.println(getPosition().getLatitude());
		System.out.println("Best Direction Angle: " + bestDirectionAngle);
		System.out.println("Min Distance: " + minDistance);
						
	}
	
	// Take reading after the move
	public String takeReading(Position dronePosition) {
		//TODO: ONLY should add the SensorPoint if the drone's position is within 0.0002,
		// and then you can add respective SensorPoint as visited and remove it from notVisited
		String sensorPointStr = "null";
		
		double distance = calculateDistance(dronePosition, nextSensorPoint.getPosition());
		if (distance < 0.0002) {
			visited.add(nextSensorPoint);
			notVisited.remove(nextSensorPoint);
			sensorPointStr = nextSensorPoint.getLocation();
		}
		return sensorPointStr;
	}
	
	// NEW: After drone reads all points, return back to original location as best as possible
	// Similar format to move()
	public void returnTowardsOriginalLocation() {
		var buildings = getNoFlyZoneBuildings();
		var droneCurrentPosition = getPosition();
		var originalPosition = getTravelledPath().get(0);
		
		// Manually create the 2D paths for the buildings
		var building1 = Map.createPath2D(buildings.get(0));
		var building2 = Map.createPath2D(buildings.get(1));
		var building3 = Map.createPath2D(buildings.get(2));
		var building4 = Map.createPath2D(buildings.get(3));
		
		// Checks for each viable direction
		double minDistance = Integer.MAX_VALUE;
		int bestDirectionAngle = 0;
		for (int directionAngle = 0; directionAngle < 360; directionAngle += 10) {
			
			// Check possible drone position
			var droneNextPosition = droneCurrentPosition.nextPosition(new Direction(directionAngle));
			double distance = calculateDistance(droneNextPosition, originalPosition);
			
			var lineStr = Map.createLine2D(droneCurrentPosition, droneNextPosition);
												
			if (meetsAllRequiredConstraints(droneNextPosition, lineStr, building1, building2, building3, building4)) {
				// Finally check for the minimal distance
				if (distance < minDistance) {
					minDistance = distance;
					bestDirectionAngle = directionAngle;
				}
			}

		}
		var newPosition = position.nextPosition(new Direction(bestDirectionAngle));
		addPositionForTravelPath(newPosition);
		// Set the drone's new location
		setPosition(newPosition);
		// Sets best previous direction angle for recording
		setAngle(bestDirectionAngle);
		
		System.out.println("Best Direction Angle: " + bestDirectionAngle);
		System.out.println("Min Distance: " + minDistance);
				
		double distance = calculateDistance(newPosition, originalPosition);
		if (distance < 0.0002) {
			setReturned();
		}
		
	}
	
	
	// Helper function which creates a String representation of the drone movement
	public String createStringMovement(int moveNumber, Position droneCurrentPosition, int bestDirectionAngle, 
			Position droneNewPosition, String sensorPointStrFormat) {
		
		// Create String text for each movement
		String movement = String.format("%s,%s,%s,%s,%s,%s,%s",
				Integer.toString(moveNumber),
				Double.toString(droneCurrentPosition.getLongitude()),
				Double.toString(droneCurrentPosition.getLatitude()),
				Integer.toString(bestDirectionAngle),
				Double.toString(droneNewPosition.getLongitude()),
				Double.toString(droneNewPosition.getLatitude()),
				sensorPointStrFormat);
		return movement;
	}
	
	public boolean isVisited(SensorPoint point) {
		// Checks if SensorPoint is read
		var visited = getVisitedSensorPoints();
		if (visited.contains(point)) {
			return true;
		}
		return false;
	}
	
	// Method calculates distance from currentPosition to originalPosition
	// Can also calculate distance from drone's position to the sensorPoint's position
	public double calculateDistance(Position currentPosition, Position originalPosition) {
		double x1 = currentPosition.getLongitude();
		double x2 = originalPosition.getLongitude();
		double y1 = currentPosition.getLatitude();
		double y2 = originalPosition.getLatitude();
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
	public List<NoFlyZoneBuilding> getNoFlyZoneBuildings() {
		return buildingsToAvoid;
	}
	public List<String> getMovements() {
		return movements;
	}
	public int getAngle() {
		return lastBestDirectionAngle;
	}
	// Used for allow drone return towards original location
	public boolean isReturned() {
		return sensoredAllPointsAndNearOriginalLocation;
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
	
	public void setAngle(int angle) {
		this.lastBestDirectionAngle = angle;
	}
	public void setReturned() {
		this.sensoredAllPointsAndNearOriginalLocation = true;
	}
	
	// For debugging purposes ONLY
	public static void main(String[] args) {
		
	}
	
}
