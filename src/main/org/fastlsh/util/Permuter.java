/*
   Copyright 2012 Michael Mastroianni, Amol Kapila, Ryan Berdeen (fastlsh.org)
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

package org.fastlsh.util;

import java.util.Random;

/** Encodes a permutation and associated methods.  In particular,
 *  it can generate a random permutation over the indices of a BitSet
 *  and can be used to permute those indices.
 */
public class Permuter
{
    Random rand = new Random();
    int [] permutation;

    public Permuter(int numBits)
    {
        permutation = initializeSortIndexArray(numBits);
        reset();
    }

    protected static int [] initializeSortIndexArray(int len)
    {
        int [] retval = new int[len];
        for(int i = 0; i < len; i++) retval[i] = i;
        return retval;
    }

    /** Recalculates a new random permutation to be stored in the object.
     */
    public void reset()
    {
        rand.nextInt();
        for (int i = 0, max = permutation.length; i < max; i++) {
          int changeIdx = i + rand.nextInt(max - i);
          int tempVal = permutation[i];
          permutation[i] = permutation[changeIdx];
          permutation[changeIdx] = tempVal;
        }
    }

    /** Permutes the bits in a BitSet.
     * 
     * @param input BitSet whose bits one wants to permute.
     * @return a BitSet containing the permuted bits.
     */
    public BitSet permute(BitSet input)
    {
        BitSet output = new BitSet(input);
        for (int i = 0, max = input.numBits; i < max; i++) {
        	if (input.get(i)) output.set(permutation[i]);
        	else output.clear(permutation[i]);
        }
        return output;
    }
}
