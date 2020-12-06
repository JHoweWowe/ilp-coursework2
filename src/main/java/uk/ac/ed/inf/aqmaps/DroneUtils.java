package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;

// Contains static helper methods of Drone class
public class DroneUtils {
	
	// Helper function: find closest not visited SensorPoint
	public static SensorPoint findClosestNotVisitedSensorPoint(Position dronePosition) {
		
		var notVisitedSensorPoints = Drone.getNotVisitedSensorPoints();
		double minDistance = Integer.MAX_VALUE;
		// Default closest sensor point is the first in list, unless if one finds closest distance
		SensorPoint closestSensorPoint = notVisitedSensorPoints.get(0);
		// Search for other sensorPoints if applicable
		for (int i = 1; i < notVisitedSensorPoints.size(); i++) {
			var sensorPoint = notVisitedSensorPoints.get(i);
			var sensorPointPosition = sensorPoint.getPosition();
			var distance = DroneUtils.calculateDistance(dronePosition, sensorPointPosition);
			if (distance < minDistance) {
				minDistance = distance;
				closestSensorPoint = sensorPoint;
			}
		}
		return closestSensorPoint;
	}
	
	// Checks if drone position is within confinement area and if it intersects any of buildings
	public static boolean meetsAllRequiredConstraints(Position droneNextPosition, Line2D.Double line, 
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
	
	// Static method as the drone should not need to be instantiated in order to obtain visited sensors
	public static boolean hasVisited(SensorPoint point) {
		// Checks if SensorPoint is read for the points 
		var visited = Drone.getVisitedSensorPoints();
		if (visited.contains(point)) {
			return true;
		}
		return false;
	}
	
	// Idea: Calculate distance between one position and another
	// Find distance between drone's currentPosition to originalPosition or drone's position to the sensorPoint's position
	public static double calculateDistance(Position p1, Position p2) {
		double diffLongitudeSquared = Math.pow(p1.getLongitude()-p2.getLongitude(), 2);
		double diffLatitudeSquared = Math.pow(p1.getLatitude()-p2.getLatitude(), 2);
		var distance = Math.sqrt(diffLongitudeSquared + diffLatitudeSquared);
		return distance;
	}
	
	// Helper function which creates a String representation of the drone movement
	public static String createStringMovement(int moveNumber, Position droneCurrentPosition, int bestDirectionAngle, 
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
