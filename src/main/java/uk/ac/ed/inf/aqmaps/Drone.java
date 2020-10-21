package uk.ac.ed.inf.aqmaps;

import java.util.List;
import java.util.ArrayList;

public class Drone {
	
	private Position position;
	private List<SensorPoint> visited;
	private List<SensorPoint> notVisited;
	
	private final double droneMovementDegrees = 0.0003;
	
	// When the drone is instantiated, drone should not have visited any points so far
	public Drone(Position position, List<SensorPoint> points) {
		this.position = position;
		visited = new ArrayList<SensorPoint>();
		this.notVisited = points;
	}
	
	// Obtain drone position
	public Position getPosition() {
		return position;
	}
	
	/** Drone methods- includes movement and take reading of sensor point **/
	
	
	
	

}
