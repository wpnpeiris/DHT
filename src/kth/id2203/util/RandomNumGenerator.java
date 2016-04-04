package kth.id2203.util;

import java.util.Random;

public class RandomNumGenerator {

	private static RandomNumGenerator instance;
	
	private Random random;
	
	private RandomNumGenerator(){
		random = new Random();
	}
	
	public static RandomNumGenerator getInstance() {
		if(instance == null) {
			instance = new RandomNumGenerator();
		}
		return instance;
	}
	
	public int next(int max) {
		return random.nextInt(max);
	}
}
