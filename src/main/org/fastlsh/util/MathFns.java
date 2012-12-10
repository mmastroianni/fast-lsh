package org.fastlsh.util;

public class MathFns {
	/**
	 * Computes the dot product between two equal-length arrays of doubles.
	 * @param x first array
	 * @param y second array
	 * @return dot product
	 */
	public static double dot(double [] x, double [] y) {
	    assert(x.length == y.length);
	    double result = 0;
		for (int i = 0, max = x.length; i < max; i++) result += x[i] * y[i];
		return result;
	}
	
	/**
	 * Computes the Euclidean (l2) distance between two equal-length arrays of doubles.
	 * @param first array
	 * @param second array
	 * @return Euclidean distance between arrays
	 */
	public static double l2Dist(double [] x, double [] y) {
        assert(x.length == y.length);
	    double result = 0;
		for (int i = 0, max = x.length; i < max; i++) result += (y[i] - x[i]) * (y[i] - x[i]);
		return Math.sqrt(result);
	}

	/**
	 * Computes the Euclidean norm of an array of doubles.
	 * @param vals array whose norm is desired
	 * @return norm of the input array
	 */
    public static double norm2(double [] vals) {
        double out = 0;
        for(int i = 0, m = vals.length; i < m; i++) out += vals[i]*vals[i];
        return Math.sqrt(out);
    }
    
    /**
     * Divides each element of an array of doubles by a non-zero scalar.
     * @param vals the array
     * @param s the scalar by which to divide the array elements
     */
    public static void scalarDivide(double [] vals, double s) {
        for(int i = 0, m = vals.length; i < m; i++) vals[i] /= s;        
    }
}
