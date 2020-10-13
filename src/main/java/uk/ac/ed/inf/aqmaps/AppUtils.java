package uk.ac.ed.inf.aqmaps;

import static java.util.Objects.isNull;

import java.io.BufferedReader;
import java.io.File;
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject; 


/**
 * Determine which methods should be determined private and public
 * Research more in detail and explain differences
 * 
 * @author Justin Howe
 *
 */

public final class AppUtils {
	
	// Main utility function for obtaining a list of SensorPoints- to be called in main class
	public static List<SensorPoint> fetchSensorPointData(String dayStr, String monthStr, String yearStr, String portStr) throws Exception {
		
		// First creates a HTTP Client- usually from web browser
		var client = HttpClient.newHttpClient();
		
		// Then create URL String from given parameters
		String urlString = AppUtils.createURLString(dayStr, monthStr, yearStr, portStr);
		
		// Create HTTPRequest
		HttpRequest request = createHttpRequest(urlString);
		
		// Send HTTPResponse
		HttpResponse<String> response = null;
		try {
			response = sendHttpResponse(client, request);
			System.out.println(response.uri());
			System.out.println(response.uri().toURL());
		} 
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		List<SensorPoint> sensorPoints = parseJson(response.uri().toURL());
		
		return sensorPoints;
	}
	
	/** Back-end methods **/
	// Creates URL String
	private static String createURLString(String dayStr, String monthStr, String yearStr, String portStr) {
		String urlString = "http://localhost:";
		urlString = urlString + portStr + "/maps/" + yearStr + "/" + monthStr + "/" + dayStr + "/";
		urlString += "air-quality-data.json";
		return urlString;
	}
	
	// Create URL String for obtaining coordinates of that String-based location after getting the SensorPoint location
	private static String createURLString2(String sensorPointLocation) {
		// Assume connection 
		String urlString = "http://localhost:9898";
		String[] words = sensorPointLocation.split("[.]");
		urlString = urlString + "/words/" + words[0] + "/" + words[1] + "/" + words[2] + "/";
		urlString += "details.json";
		return urlString;
	}
	
	private static HttpRequest createHttpRequest(String urlString) {
		
		HttpRequest request = null;
		
		try {
			request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException ("INVALID URL");
		}
		
		return request;
	}
	
	private static HttpResponse<String> sendHttpResponse(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
		HttpResponse<String> response = null;
		try {
			response = client.send(request, BodyHandlers.ofString());
			
			System.out.println(response);
			
		}
		catch (IOException e) {
			throw new IOException("IO");
		}
		catch (InterruptedException e) {
			throw new InterruptedException("We got a problem chief");
		}
		return response;
	}
	
	// Read File using InputStream
	private static BufferedReader readFile(URL url) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		return br;
	}
	
	// MAIN METHOD FOR parsing JSON to Java Object which will be added as a Point in Main function
	private static List<SensorPoint> parseJson(URL url) {
		
		BufferedReader br = null;
		
		// Create empty list of SensorPoints
		List<SensorPoint> sensorPointList = new ArrayList<SensorPoint>();
		
		// Attempt parsing the JSON String
		try {
			
			// BufferedReader should read the Air Quality Data JSON file					
			
			// = new BufferedReader(new FileReader("test-air-quality-data.json"));			
			br = AppUtils.readFile(url);
			
			// Assume JSONObject has already been created from root
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
				
				double batteryPercentage = currentObject.get("battery").getAsDouble();
				String reading = currentObject.get("reading").getAsString();
				
				SensorPoint sensorpoint = new SensorPoint(location,longitude,latitude,batteryPercentage,reading);
				sensorPointList.add(sensorpoint);
			}		
		}
		
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sensorPointList;
		
	}
    
	public static double[] fetchSensorPointLocationCoords(String sensorPointLocation) throws MalformedURLException {

		// First create HTTP client
		var client = HttpClient.newHttpClient();
		
		// Then create URL String
		String urlString = AppUtils.createURLString2(sensorPointLocation);
		
		// Create HTTPRequest
		HttpRequest request = createHttpRequest(urlString);
		
		// Send HTTPResponse
		HttpResponse<String> response = null;
		try {
			response = sendHttpResponse(client, request);
			System.out.println(response.uri());
			System.out.println(response.uri().toURL());
		} 
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		double[] coordinates = AppUtils.parseJsonCoords(response.uri().toURL());
		
		return coordinates;
		
	}
	
	// Interested in obtaining coordinates from the SensorPoint's location
	private static double[] parseJsonCoords(URL url) {
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
		
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return coordinates;
		
	}

	// Main method- for debugging purposes ONLY
	public static void main(String[] args) {
		System.out.println(AppUtils.createURLString2("slips.mass.baking"));
	}
	
}
