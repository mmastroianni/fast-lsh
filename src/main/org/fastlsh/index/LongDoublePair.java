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

import java.util.Comparator;

public class LongDoublePair
{
    public final long l;
    public final double d;
    
    public LongDoublePair(long il, double id)
    {
        l = il;
        d = id;
    }
    
    public static class DescendingDComparator implements Comparator<LongDoublePair>
    {
        @Override
        public int compare(LongDoublePair o1, LongDoublePair o2)
        {
            if(o1.d == o2.d) return o1.l ==o2.l? 0: o1.l > o2.l? -1: 1;
            return o1.d > o2.d? -1: 1;
        }
    }

    public static class AscendingDComparator implements Comparator<LongDoublePair>
    {
        @Override
        public int compare(LongDoublePair o1, LongDoublePair o2)
        {
            if(o1.d == o2.d) return o1.l ==o2.l? 0: o1.l > o2.l? 1: -1;
            return o1.d > o2.d? 1: -1;
        }
    }

    public String toString()
    {
        return "<" + l + "," + d + ">";
    }
    
    @Override
    public int hashCode() {
    	return (int) l;
    }
    
    @Override
    public boolean equals(Object other) {
    	LongDoublePair o2 = (LongDoublePair) other;
    	return (l == o2.l && d == o2.d);
    }
}
