package org.fastlsh.util;

public class MathFns {
	public static double dot(double [] x, double [] y) {
	    assert(x.length == y.length);
	    double result = 0;
		for (int i = 0, max = x.length; i < max; i++) result += x[i] * y[i];
		return result;
	}
	
	public static double l2Dist(double [] x, double [] y) {
        assert(x.length == y.length);
	    double result = 0;
		for (int i = 0, max = x.length; i < max; i++) result += (y[i] - x[i]) * (y[i] - x[i]);
		return Math.sqrt(result);
	}
	
    public static double norm2(double [] vals) {
        double out = 0;
        for(int i = 0, m = vals.length; i < m; i++) out += vals[i]*vals[i];
        return Math.sqrt(out);
    }
    
    public static void scalarDivide(double [] vals, double s) {
        for(int i = 0, m = vals.length; i < m; i++) vals[i] /= s;        
    }
}
