package uk.ac.ed.inf.aqmaps;

import java.util.List;
import java.util.ArrayList;

public class Drone {
	
	private Position position;
	// Next SensorPoint drone should move towards and take reading
	private SensorPoint nextSensorPoint;
	private List<Position> travelledPath;
	// Static attributes because DroneUtils (helper class) uses following attributes
	private static List<SensorPoint> visited;
	private static List<SensorPoint> notVisited;
	private List<NoFlyZoneBuilding> buildingsToAvoid;
	
	// String text representation for movements- used to generate text files in App.java
	private List<String> movements;
	
	// Boolean indicating if drone should return to original location- only activated after obtaining all sensor points
	private boolean sensoredAllPointsAndNearOriginalLocation;
		
	// One of the attributes required to record every movement
	private int lastBestDirectionAngle;
		
	private int numberOfMoves;
	
	private boolean isStuck;
	
	// When the drone is instantiated, drone should not have visited any points so far
	public Drone(Position position, List<SensorPoint> points, List<NoFlyZoneBuilding> buildingsToAvoid) {
		this.position = position;
		// Drone's visited Sensorpoints should be empty but notVisited should be the points given
		visited = new ArrayList<SensorPoint>();
		notVisited = points;
		// Drone's initial travel path should be completely empty
		this.travelledPath = new ArrayList<Position>();
		this.buildingsToAvoid = buildingsToAvoid;
		this.movements = new ArrayList<String>();
		
		this.lastBestDirectionAngle = 0;
		this.sensoredAllPointsAndNearOriginalLocation = false;
		this.numberOfMoves = 150;
		
		this.isStuck = false;
	}
		
	// MAIN METHOD: Flight Path- Greedy Algorithm [Somewhat efficient]
	public void generateGreedyFlightPath() {
		while (numberOfMoves > 0) {
			if (!(notVisited.isEmpty())) {
				// Searches to find NOT VISITED closest SensorPoint based from drone position and then set it
				var dronePosition = getPosition();
				var sensorPoint = DroneUtils.findClosestNotVisitedSensorPoint(dronePosition);
				setNextSensorPoint(sensorPoint);
				if (!(DroneUtils.hasVisited(sensorPoint))) {					
					// Move towards that sensor point which is not visited and take reading if applicable
					move();
					
					var newDronePosition = getPosition();
					var lastBestDirectionAngle = getAngle();
					
					String pointStr = takeReading(newDronePosition);
					
					if (isStuck == false) {
					
						// Records the generated movement String text
						int moveNumber = getMovements().size()+1;					
						String movement = DroneUtils.createStringMovement(moveNumber, dronePosition, lastBestDirectionAngle, 
								newDronePosition, pointStr);
						// Adds to the Movements function
						getMovements().add(movement);
						numberOfMoves--;
					
					}
				}
			}
			if (notVisited.isEmpty() && (!(isReturned()))) {
				var dronePosition = getPosition();
				
				//System.out.println("Drone returning towards original location");
				returnTowardsOriginalLocation();
				
				var lastBestDirectionAngle = getAngle();
				var newDronePosition = getPosition();
								
				if (isStuck == false) {
							
					// Generates the movement String text- could be moved outside of function
					int moveNumber = getMovements().size()+1;					
					String movement = DroneUtils.createStringMovement(moveNumber, dronePosition, lastBestDirectionAngle, 
							newDronePosition, "null");
					// Adds to the Movements function
					getMovements().add(movement);
					numberOfMoves--; 
				
				}
			}
			if ((notVisited.isEmpty()) && (isReturned())) {
				System.out.println("Number of Moves Remaining: " + numberOfMoves);
				// Terminate algorithm when finished
				break;
			}
		}
	}
				
	// Attempts to get the drone out of the loop of moving back and forth
	public void handleStuckError() {
				
		var path = getTravelledPath();
		
		// Manually determines drone is stuck if drone moves back and forth twice
		if (path.size() >= 4) {
			var latitude1 = Double.valueOf(path.get(path.size()-1).getLatitude());
			var longitude1 = Double.valueOf(path.get(path.size()-1).getLongitude());
			var latitude2 = Double.valueOf(path.get(path.size()-2).getLatitude());
			var longitude2 = Double.valueOf(path.get(path.size()-2).getLongitude());
			var latitude3 = Double.valueOf(path.get(path.size()-3).getLatitude());
			var longitude3 = Double.valueOf(path.get(path.size()-3).getLongitude());
			var latitude4 = Double.valueOf(path.get(path.size()-4).getLatitude());
			var longitude4 = Double.valueOf(path.get(path.size()-4).getLongitude());

			if (latitude1.equals(latitude3) && (longitude1.equals(longitude3))) {
				if (latitude2.equals(latitude4) && (longitude2.equals(longitude4))) {
					isStuck = true;
					forceMove();
				}

			}
		}
	}
	
	public void forceMove() {
		
		var buildings = getNoFlyZoneBuildings();
		
		// Manually create the boundaries for the buildings using Path2D!
		var building1 = Map.createPath2D(buildings.get(0));
		var building2 = Map.createPath2D(buildings.get(1));
		var building3 = Map.createPath2D(buildings.get(2));
		var building4 = Map.createPath2D(buildings.get(3));
		
		// The attempted angles list MUST be in order
		// When stuck, the drone is more likely to get stuck when the drone is located perpendicular to the drone building segment
		int[] attemptedAngles = {80,100,270,290,10,170,190,350,0};
		
		for (int i = 0; i < attemptedAngles.length; i++) {
			int angle = attemptedAngles[i];
			var newPosition = position.nextPosition(new Direction(angle));
			
			var lineStr = Map.createLine2D(getPosition(), newPosition);
			
			if (DroneUtils.meetsAllRequiredConstraints(newPosition, lineStr, building1, building2, building3, building4)) {
				
				// Generates the movement String text
				int moveNumber = getMovements().size()+1;					
				String movement = DroneUtils.createStringMovement(moveNumber, getPosition(), angle, 
						newPosition, "null");
				// Adds to the Movements function
				getMovements().add(movement);
				numberOfMoves--;
				
				addPositionForTravelPath(newPosition);
				// Set the drone's new location
				setPosition(newPosition);
				// Sets best previous direction angle for recording
				setAngle(angle);
				
				var newPosition2 = newPosition.nextPosition(new Direction(angle));
				moveNumber = getMovements().size()+1;					
				movement = DroneUtils.createStringMovement(moveNumber, getPosition(), angle, 
						newPosition2, "null");
				// Adds to the Movements function
				getMovements().add(movement);
				numberOfMoves--;
				
				addPositionForTravelPath(newPosition2);
				setPosition(newPosition2);
				setAngle(angle);
				
				var newPosition3 = newPosition2.nextPosition(new Direction(angle));
				moveNumber = getMovements().size()+1;
				movement = DroneUtils.createStringMovement(moveNumber, getPosition(), angle,
						newPosition3, "null");
				getMovements().add(movement);
				numberOfMoves--;
				
				addPositionForTravelPath(newPosition3);
				setPosition(newPosition3);
				setAngle(angle);
				
				break;
				
			}						

		}

	}

	
	/** Drone methods- includes movement and take reading of sensor point **/
	// Searches and determines which direction should the drone fly in 
	// FOR each move AFTER knowing which sensor point to go to
	
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
		
		isStuck = false;
		
		// Check if drone is stuck- force movement twice (should be refactored)
		// Should also create the text movement for the additional forced movement
		handleStuckError();
		
		if (isStuck() == false) {
			// Checks for each viable direction
			double minDistance = Integer.MAX_VALUE;
			int bestDirectionAngle = 0;
			for (int directionAngle = 0; directionAngle < 360; directionAngle += 10) {
				
				// Check possible drone position
				var droneNextPosition = droneCurrentPosition.nextPosition(new Direction(directionAngle));
				double distance = DroneUtils.calculateDistance(droneNextPosition, nextSensorPoint.getPosition());
				
				var lineStr = Map.createLine2D(droneCurrentPosition, droneNextPosition);
													
				if (DroneUtils.meetsAllRequiredConstraints(droneNextPosition, lineStr, building1, building2, building3, building4)) {
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
			//System.out.println(getPosition().getLongitude());
			//System.out.println(getPosition().getLatitude());
			//System.out.println("Best Direction Angle: " + bestDirectionAngle);
			//System.out.println("Min Distance: " + minDistance);
		}
								
	}
	
	// Take reading after the move
	public String takeReading(Position dronePosition) {
		String sensorPointStr = "null";
		
		double distance = DroneUtils.calculateDistance(dronePosition, nextSensorPoint.getPosition());
		// Only take reading if distance between drone's position and next sensor is within 0.0002 degrees
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
						
		// Manually create the 2D path boundaries for the buildings
		var building1 = Map.createPath2D(buildings.get(0));
		var building2 = Map.createPath2D(buildings.get(1));
		var building3 = Map.createPath2D(buildings.get(2));
		var building4 = Map.createPath2D(buildings.get(3));
		
		isStuck = false;
		
		// Check if drone is stuck- force movement twice (should be refactored)
		// Should also create the text movement for the additional forced movement
		handleStuckError();
				
		if (isStuck() == false) {
			// Checks for each viable direction
			double minDistance = Integer.MAX_VALUE;
			int bestDirectionAngle = 0;
			for (int directionAngle = 0; directionAngle < 360; directionAngle += 10) {
				
				// Check possible drone position
				var droneNextPosition = droneCurrentPosition.nextPosition(new Direction(directionAngle));
				double distance = DroneUtils.calculateDistance(droneNextPosition, originalPosition);
				
				var lineStr = Map.createLine2D(droneCurrentPosition, droneNextPosition);
													
				if (DroneUtils.meetsAllRequiredConstraints(droneNextPosition, lineStr, building1, building2, building3, building4)) {
					// Finally check for the minimal distance
					if (distance < minDistance) {
						minDistance = distance;
						bestDirectionAngle = directionAngle;
					}
				}
			}
			
			var newPosition = position.nextPosition(new Direction(bestDirectionAngle));
			addPositionForTravelPath(newPosition);
			setPosition(newPosition);
			setAngle(bestDirectionAngle);
			
			// Debugging purposes only
			//System.out.println("Best Direction Angle: " + bestDirectionAngle);
			//System.out.println("Min Distance: " + minDistance);
					
			double distance = DroneUtils.calculateDistance(newPosition, originalPosition);
			if (distance < 0.0002) {
				setReturned();
			}

		}
						
	}	
	
	/** GETTER METHODS **/
	// Obtain drone position
	public Position getPosition() {
		return position;
	}
	public SensorPoint getNextSensorPoint() {
		return nextSensorPoint;
	}
	public static List<SensorPoint> getVisitedSensorPoints() {
		return visited;
	}
	public static List<SensorPoint> getNotVisitedSensorPoints() {
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
	
	public boolean isStuck() {
		return isStuck;
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
