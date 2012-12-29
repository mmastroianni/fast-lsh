package org.fastlsh.threshold;

import org.fastlsh.util.MathFns;

public class L2Threshold implements ScoreThreshold {
	double threshold;
	
	public L2Threshold(double threshold) {
		if (threshold < 0)
			throw new RuntimeException("Threshold needs to be non-negative.");
		this.threshold = threshold;
	}

	@Override
	public double score(double[] x, double[] y) {
		return MathFns.l2Dist(x, y);
	}

	@Override
	public boolean threshold(double score) {
		return score <= threshold;
	}

}
