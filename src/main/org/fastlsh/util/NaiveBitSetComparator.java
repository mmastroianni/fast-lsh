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

public class NaiveBitSetComparator implements Comparator<Signature>
{
    @Override
    public int compare(Signature bs1, Signature bs2)
    {
        BitSet bitset1 = bs1.bits;
        BitSet bitset2 = bs2.bits;
    	for (int i = 0; i < 127; i++) {
    		if (bitset1.get(i) && !bitset2.get(i))
    			return 1;
    		else if (!bitset1.get(i) && bitset2.get(i))
    			return -1;
    	}
    	return 0;
    }
}
