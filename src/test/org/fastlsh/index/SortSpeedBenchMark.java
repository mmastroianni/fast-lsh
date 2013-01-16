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
package org.fastlsh.index;

import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.fastlsh.util.BitSet;
import org.fastlsh.util.Signature;
import org.fastlsh.util.LexicographicBitSetComparator;
import org.fastlsh.util.Permuter;

public class SortSpeedBenchMark
{
    public static Signature [] makeRandomBitSets(int numBitSets, int cardinality)
    {
        List<Signature> bitsetList = new ArrayList<Signature>(numBitSets);
        int curId = 1;
        
        Random rand = new Random();
        do
        {
            BitSet bs = new BitSet(cardinality);
            for(int i = 0; i < cardinality; i++)
            {
                if(rand.nextBoolean()) bs.set(i);
            }
            bitsetList.add(new Signature(curId, bs));
            curId++;
        }
        while(curId <= numBitSets);
        return bitsetList.toArray(new Signature[bitsetList.size()]);
    }
    
    public long [] getSimilars(Signature search, int beamRadius, Signature [] items, Comparator<Signature> comparator)
    {
        TLongArrayList retval = new TLongArrayList();
        
        int idx = Arrays.binarySearch(items, search, comparator);
        int bottom = Math.max(idx-beamRadius, 0);
        int top = Math.min(idx+beamRadius, items.length);
        
        for(int i = bottom; i < top; i++)
        {
            retval.add(items[i].id);
        }
        return retval.toArray();
    }
    
    public static void main(String [] args)
    {
        int numBitSets = Integer.parseInt(args[0]);
        int cardinality = Integer.parseInt(args[1]);
        long start = System.currentTimeMillis();        
        Signature [] bitSets = makeRandomBitSets(numBitSets, cardinality);
        long end = System.currentTimeMillis();
        System.out.println("Took: " + (end - start) + " millis to initialize " + numBitSets + " bitsets of cardinality " + cardinality + ".");

        Permuter permuter = new Permuter(cardinality);
        PermutationIndexWriter.permuteAndSort(permuter, bitSets);
        LexicographicBitSetComparator comparator = new LexicographicBitSetComparator();
        start = System.currentTimeMillis();
        Arrays.sort(bitSets, comparator);
        end = System.currentTimeMillis();
        
        System.out.println("Took: " + (end - start) + " millis to sort " + numBitSets + " bitsets of cardinality " + cardinality + ".");
    }
    
}
