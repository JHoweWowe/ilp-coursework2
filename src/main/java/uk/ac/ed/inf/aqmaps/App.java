package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

/**
 * This is the main application
 *
 */
public class App {
	
    private static final Point gridPointNW = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point gridPointNE = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point gridPointSW = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point gridPointSE = Point.fromLngLat(-3.184319, 55.942617);
	
    public static void main( String[] args ) {
    	
        
    	
    	
    	
    	// Then create the no-fly zones as Polygons
    	
    	
    	
        // Ultimate Goal- Create Feature Collection JSON format to generate the whole application
    	
    	// First let's create the confinement area as a LineString
        // List of Coordinates must be added in following order, follows right-hand rule 
        var gridPoints = new ArrayList<Point>();
        gridPoints.add(gridPointNW);
        gridPoints.add(gridPointNE);
        gridPoints.add(gridPointSE);
        gridPoints.add(gridPointSW);
        gridPoints.add(gridPointNW);
                        
        // Create the map's boundaries 
        var mapBoundary = LineString.fromLngLats(gridPoints);
        var feature = Feature.fromGeometry(mapBoundary);
            	
    	var features = new ArrayList<Feature>();
    	features.add(feature);
    	
        var featureCollection = FeatureCollection.fromFeatures(features);
        
        // Ultimate Goal- generate a JSON map
        System.out.println(featureCollection.toJson());
    	
    }
}
