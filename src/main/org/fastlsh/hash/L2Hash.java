/*
   Copyright 2012 Michael Mastroianni, Amol Kapila (fastlsh.org)
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fastlsh.hash;

import java.io.Serializable;
import java.util.Random;

import org.fastlsh.index.VectorWithId;

/** This class is designed to implement an LSH hash function for
 *  Euclidean (l2) distance.  Howveer, we only hypothesize that this might work.
 *  It has not yet been tested.  It will be tested, and if it works,
 *  will be included in future versions.
 */
public class L2Hash implements HashFunction, Serializable
{
	private static final long serialVersionUID = 7514192857280806912L;
	
    double [] projection;
    double offset;
    double binWidth;
    
    public L2Hash(int dimension, Random rand) {
        projection = new double[dimension];
        for (int i = 0; i < dimension; i++) {
        	projection[i] = rand.nextGaussian();
        }
    }
    
    public L2Hash(int dimension) {
        this(dimension, new Random());
    }

    @Override
    public boolean hash(VectorWithId input) {
    	return ( ((input.dotProduct(projection) - offset) / binWidth) % 2 ) == 0;
    }
}
