package org.fastlsh.util;

public class MathFns {
	/**
	 * These functions assume that vectors x and y are of the same length.
	 * We bypass checking this in favor of speed.
	 * @param x
	 * @param y
	 * @return
	 */
	
	public static double dot(double [] x, double [] y) {
		double result = 0;
		for (int i = 0, max = x.length; i < max; i++) {
			result += x[i] * y[i];
		}
		return result;
	}
	
	// TODO:
	// pending discussion of design issues:
//	public static double dot(SparseVector x, SparseVector y) {
//		
//	}
	
	public static double l2Dist(double [] x, double [] y) {
		double result = 0;
		for (int i = 0, max = x.length; i < max; i++) {
			result += (y[i] - x[i]) * (y[i] - x[i]);
		}
		return Math.sqrt(result);
	}
	
	// TODO: Hamming distance.  Make efficient in terms of bit operations.
}
