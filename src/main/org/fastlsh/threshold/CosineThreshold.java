package org.fastlsh.threshold;

import org.fastlsh.util.MathFns;

public class CosineThreshold implements ScoreThreshold {
	private double threshold;
	
	public CosineThreshold(double threshold) {
		if (threshold < -1 || threshold > 1)
			throw new RuntimeException("Threshold needs to be between -1 and 1, inclusive.");
		this.threshold = threshold;
	}

	@Override
	public double score(double[] x, double[] y) {
		return MathFns.dot(x,  y);
	}
	
	@Override
	public boolean threshold(double score) {
		return score >= threshold;
	}
}
