package org.fastlsh.index;

import java.util.Random;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;

public class ColtVsCustomDotProduct {
	public static void main(String [] args) {
		int numPoints = Integer.parseInt(args[0]);
		int dimension = Integer.parseInt(args[1]);
		
		// generate data:
		double result = 0;
		Random rand = new Random();
		double [][] data = new double[numPoints][];
		for (int i = 0; i < numPoints; i++) {
			data[i] = new double[dimension];
			for (int j = 0; j < dimension; j++) {
				result = data[i][j] = rand.nextDouble();
			}
		}
		
		// calculate dot products using custom dot product:
		long begin = System.currentTimeMillis();
		for (int i = 0; i < numPoints; i++) {
			for (int j = 0; j < numPoints; j++) {
				result = dot(data[i], data[j]);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Custom dot product took " + (end-begin) + " ms.");
		System.out.println("Result: " + result);
		
		// calculate dot products using COLT:
		DenseDoubleMatrix1D [] dataColt = new DenseDoubleMatrix1D[numPoints];
		for (int i = 0, max = dataColt.length; i < max; i++) {
			dataColt[i] = new DenseDoubleMatrix1D(data[i]);
		}
		begin = System.currentTimeMillis();
		for (int i = 0; i < numPoints; i++) {
			for (int j = 0; j < numPoints; j++) {
				dataColt[i].zDotProduct(dataColt[j]);
			}
		}
		end = System.currentTimeMillis();
		System.out.println("COLT dot product took " + (end-begin) + " ms.");
	}
	
	private static double dot (double [] x, double [] y) {
		double result = 0;
		for (int i = 0, max = x.length; i < max; i++) {
			result += x[i] * y[i];
		}
		return result;
	}
}
