package org.fastlsh.index;

import java.io.Serializable;

import org.fastlsh.hash.HashFamily;

public class IndexOptions implements Serializable {
	private static final long serialVersionUID = 5351666720373422637L;

	public int vectorDimension;
	public int numHashes;
	public HashFamily hashFamily;
}
