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
	
	// Currently unsure if it should be in this class?
	// Obtains list of NoFlyZoneBuildings to avoid
	private List<NoFlyZoneBuilding> buildingsToAvoid;
	
	// String text representation for movements- used for FileCreator class to generate text files
	private List<String> movements;
	
	// 
	private int lastBestDirectionAngle;
	
	private final double droneMovementDegrees = 0.0003;
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
	
	// This would be useful for checking if the possible LineString goes through the Polygons
	// Test function: Check if any intersecting lines go through-- add parameters as necessary	
	// Assume number of buildings is 4	
	
	public Line2D.Double createLine2D(Position p1, Position p2) {
		var point1 = new Point2D.Double(p1.getLongitude(), p1.getLatitude());
		var point2 = new Point2D.Double(p2.getLongitude(), p2.getLatitude());
		var line = new Line2D.Double(point1, point2);
		return line;
	}
	
	public Path2D.Double createPath2D(NoFlyZoneBuilding building) {
		var path = new Path2D.Double();
		var coordinates = building.getCoordinates();
		path.moveTo(coordinates.get(0).longitude(), coordinates.get(0).latitude());
		for (int i = 1; i < coordinates.size(); i++) {
			path.lineTo(coordinates.get(i).longitude(), coordinates.get(i).latitude());
		}
		path.closePath();
		return path;
	}
	
	// Algorithm implemented with following pseudocode from StackOverFLow
	public boolean intersects(Path2D.Double path, Line2D line) {
	    double x1 = -1 ,y1 = -1 , x2= -1, y2 = -1;
	    for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) 
	    {
	        double[] coordinates = new double[6];
	        switch (pi.currentSegment(coordinates))
	        {
	        case PathIterator.SEG_MOVETO:
	        case PathIterator.SEG_LINETO:
	        {
	            if(x1 == -1 && y1 == -1 )
	            {
	                x1= coordinates[0];
	                y1= coordinates[1];
	                break;
	            }               
	            if(x2 == -1 && y2 == -1)
	            {
	                x2= coordinates[0];             
	                y2= coordinates[1];
	                break;
	            }
	            break;
	        }
	        }
	        if(x1 != -1 && y1 != -1 && x2 != -1 && y2 != -1)
	        {
	            Line2D segment = new Line2D.Double(x1, y1, x2, y2);
	            if (segment.intersectsLine(line)) 
	            {
	                return true;
	            }
	            x1 = -1;
	            y1 = -1;
	            x2 = -1;
	            y2 = -1;
	        }
	    }
	    return false;
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
		
		// Checks for each viable direction
		double minDistance = Integer.MAX_VALUE;
		int bestDirectionAngle = 0;
		for (int directionAngle = 0; directionAngle < 360; directionAngle += 10) {
			
			// Check possible drone position
			var droneNextPosition = droneCurrentPosition.nextPosition(new Direction(directionAngle));
			double distance = calculateDistance(droneNextPosition, nextSensorPoint);
			
			var lineStr = createLine2D(droneCurrentPosition, droneNextPosition);
			
			// Manually create the 2D paths for the buildings
			var building1 = createPath2D(buildings.get(0));
			var building2 = createPath2D(buildings.get(1));
			var building3 = createPath2D(buildings.get(2));
			var building4 = createPath2D(buildings.get(3));
									
			// TODO: Also check the boundaries for movement..it ultimately determines whether
			// it can be assigned
			// First checks if drone's next anticipated position is within confinement area
			if (droneNextPosition.isWithinConfinementArea()) {
				
				// Also checks if the drone next position is NOT in any of the Buildings
				if (!((droneNextPosition.isWithinAnyFlyZoneBuilding(buildings)))) {
					
					// Line intersects function
					if (!(intersects(building1, lineStr))) {
						if (!(intersects(building2, lineStr))) {
							if (!(intersects(building3, lineStr))) {
								if (!(intersects(building4, lineStr))) {
									
									// Finally check for the minimal distance
									if (distance < minDistance) {
										minDistance = distance;
										bestDirectionAngle = directionAngle;
									}
									
								}
							}
						}
					}
						
				}
				
			}

		}
		var newPosition = position.nextPosition(new Direction(bestDirectionAngle));
		addPositionForTravelPath(newPosition);
		// Set the drone's new location
		setPosition(newPosition);
		
		// Test function- 
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
		
		double distance = calculateDistance(dronePosition, nextSensorPoint);
		if (distance < 0.0002) {
			visited.add(nextSensorPoint);
			notVisited.remove(nextSensorPoint);
			sensorPointStr = nextSensorPoint.getLocation();
		}
		return sensorPointStr;
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
	public List<NoFlyZoneBuilding> getNoFlyZoneBuildings() {
		return buildingsToAvoid;
	}
	public List<String> getMovements() {
		return movements;
	}
	public int getAngle() {
		return lastBestDirectionAngle;
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
	
	// For debugging purposes ONLY
	public static void main(String[] args) {
		
	}
	

}
