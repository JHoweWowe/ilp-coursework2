package uk.ac.ed.inf.aqmaps;

import static java.util.Objects.isNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
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


// Private methods to be adapted for privacy purposes- explain more in report

public final class AppUtils {
	
	// Parsing JSON to Java Object which will be added as a Point in Main function
	private static List<SensorPoint> parseJson() {
		Gson gson = new Gson();
		BufferedReader br = null;
		
		// Create empty list of SensorPoints
		List<SensorPoint> sensorPointList = new ArrayList<SensorPoint>();
		
		// Attempt parsing the JSON String
		try {
			// BufferedReader should read the Air Quality Data- should be somewhere else in the future
			br = new BufferedReader(new FileReader("test-air-quality-data.json"));
			
			// Assume JSONObject has already been created from root
			JsonArray jArray = new Gson().fromJson(br, JsonArray.class);
			
			// Assume there will always be 33 points to be read
			for (int i = 0; i < 33; i++) {
				// Obtain current location
				JsonObject currentObject = jArray.get(i).getAsJsonObject();
				
				// NOTE WARNING: JSONObject can return null- please check
				String location = currentObject.get("location").getAsString();
				double batteryPercentage = currentObject.get("battery").getAsDouble();
				String reading = currentObject.get("reading").getAsString();
				
				SensorPoint sensorpoint = new SensorPoint(location,batteryPercentage,reading);
				sensorPointList.add(sensorpoint);
			}
			
		}
		
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return sensorPointList;
		
	}
    
		
	// Main method for now- should be divided into further classes
	public static void main(String[] args) {
		
		parseJson();
		
		// Leave urlString default for now
		String urlString = "http://localhost:9898";
		
		var client = HttpClient.newHttpClient();
		
		try {
			var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
			var response = client.send(request, BodyHandlers.ofString());
						
			System.out.println(response);
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException ("URL is invalid!");
		} 
		catch (IOException | InterruptedException e) {
			System.out.println("BAD RESPONSE");
		}

	}

}
