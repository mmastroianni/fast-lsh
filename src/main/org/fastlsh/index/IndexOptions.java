package org.fastlsh.index;

import java.io.Serializable;

import org.fastlsh.hash.HashFamily;

public class IndexOptions implements Serializable {
	private static final long serialVersionUID = 5351666720373422637L;

	public int vectorDimension = -1;//we can test for this value to check whether this has been initialized;
	public int numHashes = -1;//we can test for this value to check whether this has been initialized;
	public HashFamily hashFamily = null;
	public int numPermutations = -1;//we can test for this value to check whether this has been initialized
}
