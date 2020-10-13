package uk.ac.ed.inf.aqmaps;

public class SensorPoint {
	
	private String location;
	private double longitude;
	private double latitude;
	private double batteryPercentage;
	private String sensorReading;
	
	// Constructor
	public SensorPoint(String location, double longitude, double latitude, double batteryPercentage, String sensorReading) {
		this.location = location;
		this.longitude = longitude;
		this.latitude = latitude;
		this.batteryPercentage = batteryPercentage;
		this.sensorReading = sensorReading;
	}
	
	// Getter methods
	
	public String getLocation() {
		return location;
	}
	public double getLongitude() {
		return longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public double getBatteryPercentage() {
		return batteryPercentage;
	}
	public String getSensorReading() {
		return sensorReading;
	}
	
	// Additional setter methods- if needed

}
