package uk.ac.ed.inf.aqmaps;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.io.FileReader; 
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon; 


/**
 * Application Utility class which handles the back-end of the web application
 * especially includes connect to HTTP Server respectively.
 * Class also includes parsing the Json files because it requires the response from the server to be successful
 *  
 * @author Justin Howe
 *
 */

public final class AppUtils {
		
	/** 3 helper methods for creating URL string for obtaining details for NoFlyZone buildings, 
	 * SensorPoints AQ details and coordinates for SensorPoints **/
	// Creates URL String for obtaining No-Fly-Zones buildings
	private static String createURLStringForBuildings() {
		String urlString = "http://localhost:9898/buildings/no-fly-zones.geojson";
		return urlString;
	}
	
	// Creates URL String for obtaining the SensorPoints AQ details
	private static String createURLStringForSensorPointsAirQualityData(String dayStr, String monthStr, String yearStr, String portStr) {
		String urlString = "http://localhost:";
		urlString = urlString + portStr + "/maps/" + yearStr + "/" + monthStr + "/" + dayStr + "/";
		urlString += "air-quality-data.json";
		return urlString;
	}
	
	// Create URL String for obtaining coordinates of that String-based location after getting the SensorPoint location
	private static String createURLStringForSensorPointLocationDetails(String sensorPointLocation) {
		String urlString = "http://localhost:9898";
		String[] words = sensorPointLocation.split("[.]");
		urlString = urlString + "/words/" + words[0] + "/" + words[1] + "/" + words[2] + "/";
		urlString += "details.json";
		return urlString;
	}
	
	/** Helper back-end methods to create and send HttpRequest respectively **/
	// Creates HttpRequest using the URL given in String format
	private static HttpRequest createHttpRequest(String urlString) {	
		HttpRequest request = null;
		try {
			request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
		}
		catch (IllegalArgumentException ex) {
			System.err.println("Exception occurred: " + ex.getMessage());
			System.exit(1);
		}
		return request;
	}
	
	private static HttpResponse<String> sendHttpResponse(HttpClient client, HttpRequest request) throws Exception {
		HttpResponse<String> response = null;
		try {
			response = client.send(request, BodyHandlers.ofString());
			//System.out.println(response);
		}
		catch (Exception e) {
			System.err.println("Exception occurred: " + e.getMessage());
			System.exit(1);
		}
		return response;
	}
	
	// Read File using InputStream
	private static BufferedReader readFile(URL url) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		return br;
	}
	
	/** 3 helper methods which parses JSON files, each with a different purpose **/
	// Parse JSON file for No-Fly-Zone buildings
	private static List<NoFlyZoneBuilding> parseGeoJsonBuildings(URL url) {
		BufferedReader br = null;
		//Create empty arraylist of NoFlyZoneBuildings
		List<NoFlyZoneBuilding> buildings = new ArrayList<NoFlyZoneBuilding>();
		// Try GeoJSON parsing if time allows
		try {
			br = AppUtils.readFile(url);
			JsonObject jObject = new Gson().fromJson(br, JsonObject.class);
			JsonArray jArray = jObject.get("features").getAsJsonArray();
			
			for (int i = 0; i < 4; i++) {
				JsonObject currentObject = jArray.get(i).getAsJsonObject();
				//System.out.println(currentObject);
				JsonObject properties = currentObject.get("properties").getAsJsonObject();
				//System.out.println(properties);
				String name = properties.get("name").getAsString();
				//System.out.println(name);
				JsonObject geometry = currentObject.get("geometry").getAsJsonObject();
				//System.out.println(geometry);
				JsonArray array = geometry.get("coordinates").getAsJsonArray();
				//System.out.println(array);
				//System.out.println(array.get(0).getAsJsonArray());
				//System.out.println(array.get(0).getAsJsonArray().size());
				List<Point> coordinates = new ArrayList<Point>();
				
				for (int j = 0; j < array.get(0).getAsJsonArray().size(); j++) {
					double lng = array.get(0).getAsJsonArray().get(j).getAsJsonArray().get(0).getAsDouble();
					double lat = array.get(0).getAsJsonArray().get(j).getAsJsonArray().get(1).getAsDouble();
					var point = Point.fromLngLat(lng, lat);
					coordinates.add(point);
				}
				
				// Create new building
				NoFlyZoneBuilding building = new NoFlyZoneBuilding(name, coordinates);
				buildings.add(building);
			}
			return buildings;
		}
		catch (IOException e) {
			System.err.println("Exception occurred: " + e.getMessage());
			System.exit(1);
		}
		return buildings;
	}
	
	// MAIN METHOD FOR deserializing the air quality data into a list of Sensor Points
	private static List<SensorPoint> parseJsonAirQualityData(URL url) throws Exception {
		BufferedReader br = null;
		// Create empty list of SensorPoints
		List<SensorPoint> sensorPointList = new ArrayList<SensorPoint>();
		
		// Attempt parsing the JSON String
		try {
			// BufferedReader should read the Air Quality Data JSON file					
			br = AppUtils.readFile(url);
			// JSONArray has already been created from root
			JsonArray jArray = new Gson().fromJson(br, JsonArray.class);

			// Assume there will always be 33 points to be read
			for (int i = 0; i < 33; i++) {
				// Obtain current location
				JsonObject currentObject = jArray.get(i).getAsJsonObject();
				// NOTE WARNING: JSONObject can return null- please check
				String location = currentObject.get("location").getAsString();
				
				// From the location, obtain coordinates and assign them to SensorPoint
				double[] coordinates = AppUtils.fetchSensorPointLocationCoords(location);
				
				double longitude = coordinates[0];
				double latitude = coordinates[1];
				
				var batteryPercentage = currentObject.get("battery").getAsDouble();
				var reading = currentObject.get("reading").getAsString();
				
				Position position = new Position(longitude, latitude);
				
				SensorPoint sensorpoint = new SensorPoint(location,position,batteryPercentage,reading);
				sensorPointList.add(sensorpoint);
			}		
		}
		catch (Exception e) {
			System.err.println("Exception occurred: " + e.getMessage());
			System.exit(1);
		}
		
		return sensorPointList;
		
	}
    
	// Interested in obtaining coordinates from the SensorPoint's location
	private static double[] parseJsonSensorPointLocation(URL url) throws Exception {
		
		// Assume coordinates array has length of 2
		BufferedReader br = null;
		double[] coordinates = new double[2];
		
		try {
			// Note: BufferedReader should read details.json somewhere else
			//br = new BufferedReader(new FileReader("details.json"));
			br = AppUtils.readFile(url);
			
			JsonObject jObject = new Gson().fromJson(br, JsonObject.class);
			
			var coordinatesJson = jObject.getAsJsonObject("coordinates");
			
			double longitude = coordinatesJson.get("lng").getAsDouble();
			double latitude = coordinatesJson.get("lat").getAsDouble();
			
			coordinates[0] = longitude;
			coordinates[1] = latitude;
		}
		
		catch (Exception e) {
			System.err.println("Exception occurred: " + e.getMessage());
			System.exit(1);
		}
		
		return coordinates;
		
	}

	// Main utility function for obtaining Sensor Point location coordinates
	public static double[] fetchSensorPointLocationCoords(String sensorPointLocation) throws Exception {

		// First create HTTP client
		var client = HttpClient.newHttpClient();
		
		// Then create URL String
		String urlString = AppUtils.createURLStringForSensorPointLocationDetails(sensorPointLocation);
		
		// Create HTTPRequest
		HttpRequest request = createHttpRequest(urlString);
		
		// Send HTTPResponse
		HttpResponse<String> response = null;
		
		try {
			response = sendHttpResponse(client, request);
			//System.out.println(response.uri());
			//System.out.println(response.uri().toURL());
		} 
		catch (Exception e) {
			System.err.println("Error occurred: " + e.getMessage());
			System.exit(1);
		}
		
		double[] coordinates = AppUtils.parseJsonSensorPointLocation(response.uri().toURL());
		
		return coordinates;
		
	}
	
	// Main utility function for obtaining a list of SensorPoints- to be called in main class
	public static List<SensorPoint> fetchSensorPointData(String dayStr, String monthStr, String yearStr, String portStr) throws Exception {
		
		var client = HttpClient.newHttpClient();
		
		var urlString = AppUtils.createURLStringForSensorPointsAirQualityData(dayStr, monthStr, yearStr, portStr);
		
		HttpRequest request = createHttpRequest(urlString);
		
		HttpResponse<String> response = null;

		try {
			response = sendHttpResponse(client, request);
			//System.out.println(response.uri());
			//System.out.println(response.uri().toURL());
		} 
		catch (Exception e) {
			System.err.println("Exception occurred: " + e.getMessage());
			System.exit(1);
		}
		
		List<SensorPoint> sensorPoints = parseJsonAirQualityData(response.uri().toURL());
		
		return sensorPoints;
	}

	// Main utility function for obtaining list of NoFlyZoneBuildings
	public static List<NoFlyZoneBuilding> fetchBuildingCoordinates() throws Exception {
		
		var client = HttpClient.newHttpClient();
		
		String urlString = AppUtils.createURLStringForBuildings();
		
		HttpRequest request = createHttpRequest(urlString);
		
		HttpResponse<String> response = null;
		try {
			response = sendHttpResponse(client, request);
		}
		catch (Exception e) {
			System.err.println("Exception occurred" + e.getMessage());
			System.exit(1);
		}
		
		List<NoFlyZoneBuilding> buildings = parseGeoJsonBuildings(response.uri().toURL());
		
		return buildings;
		
	}
	
	// Main method- for debugging purposes ONLY
	public static void main(String[] args) throws Exception {
		// Create URL String
		//System.out.println(AppUtils.fetchBuildingCoordinates());
	}
	
}
