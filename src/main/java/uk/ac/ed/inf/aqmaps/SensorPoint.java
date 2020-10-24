package uk.ac.ed.inf.aqmaps;

public class SensorPoint {
	
	private String location;
	private Position position;
	private double batteryPercentage;
	private String sensorReading;
	
	// Constructor
	public SensorPoint(String location, Position position, double batteryPercentage, String sensorReading) {
		this.location = location;
		this.position = position;
		this.batteryPercentage = batteryPercentage;
		this.sensorReading = sensorReading;
	}
	
	// Getter methods
	
	public String getLocation() {
		return location;
	}
	public Position getPosition() {
		return position;
	}
	public double getBatteryPercentage() {
		return batteryPercentage;
	}
	public String getSensorReading() {
		return sensorReading;
	}
	
	// Additional setter methods- if needed

}
