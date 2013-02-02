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

import java.util.Comparator;

/** This class contains a long and a double.  It encodes a near neighbor
 *  by storing the neighbor's id and the similarity between it and the query point.
 */
public class Neighbor
{
    public final long id;
    public final double score;
    
    public Neighbor(long id, double score)
    {
        this.id = id;
        this.score = score;
    }
    
    public static class SimilarityComparator implements Comparator<Neighbor>
    {
        @Override
        public int compare(Neighbor o1, Neighbor o2)
        {
            if(o1.score == o2.score) return o1.id ==o2.id? 0: o1.id > o2.id? -1: 1;
            return o1.score > o2.score? -1: 1;
        }
    }

    public static class DissimilarityComparator implements Comparator<Neighbor>
    {
        @Override
        public int compare(Neighbor o1, Neighbor o2)
        {
            if(o1.score == o2.score) return o1.id ==o2.id? 0: o1.id > o2.id? 1: -1;
            return o1.score > o2.score? 1: -1;
        }
    }

    public String toString()
    {
        return "<" + id + "," + score + ">";
    }
    
    @Override
    public int hashCode() {
    	return (int) id;
    }
    
    @Override
    public boolean equals(Object other) {
    	Neighbor o2 = (Neighbor) other;
    	return (id == o2.id && score == o2.score);
    }
}
