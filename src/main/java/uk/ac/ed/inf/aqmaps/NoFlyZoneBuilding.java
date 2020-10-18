package uk.ac.ed.inf.aqmaps;

import java.util.List;

import com.mapbox.geojson.Point;

public class NoFlyZoneBuilding {
	
	private String name;
	private List<Point> coordinates;
	
	public NoFlyZoneBuilding(String name, List<Point> coordinates) {
		this.name = name;
		this.coordinates = coordinates;
	}
	
	// Getter Methods
	public String getName() {
		return name;
	}
	public List<Point> getCoordinates() {
		return coordinates;
	}

}
