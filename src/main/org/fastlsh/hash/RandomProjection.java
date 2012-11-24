/*
   Copyright 2012 Michael Mastroianni, Amol Kapile (fastlsh.org)
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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

public class RandomProjection implements HashFunction, Serializable
{
	private static final long serialVersionUID = -2460318038698014925L;
	
	DoubleMatrix1D projection;
    
    public RandomProjection(int size) {
        this(size, new Random());
    }
    
    public RandomProjection(int size, Random rand) {
        projection = new DenseDoubleMatrix1D(size);
        for (int i = 0; i < size; i++) {
            projection.set(i,rand.nextGaussian());
        }
    }

    @Override
    public boolean hash(DoubleMatrix1D input)
    {
        return projection.zDotProduct(input) >= 0;
    }
}
