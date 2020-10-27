package uk.ac.ed.inf.aqmaps;

public class Direction {
	
	private int degrees;
	
	public Direction(int degrees) {
		if (degrees % 10 != 0) {
			throw new IllegalArgumentException("Drone must fly in multiples of 10");
		}
		else if (degrees < 0 || degrees > 350) {
			throw new IllegalArgumentException("Please enter a valid number between 0-350 which is a multiple of 10");
		}
		else {
			this.degrees = degrees;
		}
	}
	
	public int getDirectionInDegrees() {
		return degrees;
	}

}
