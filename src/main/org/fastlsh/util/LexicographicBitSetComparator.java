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

public class LexicographicBitSetComparator implements Comparator<BitSetWithId>
{
    public static boolean isLessThanUnsigned(long n1, long n2)
    {
        return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
    }

    @Override
    public int compare(BitSetWithId bs1, BitSetWithId bs2)
    {
        int numBits = bs1.bits.numBits;
        if (numBits != bs2.bits.numBits)
            throw new RuntimeException(
                    "Can't compare bitsets with differing numbers of bits: "
                            + bs1.bits.numBits + " vs " + bs2.bits.numBits
                            + ".");
        long[] longs = bs1.bits.bits;
        long[] longs2 = bs2.bits.bits;
        int longsMaxIdx = longs.length - 1;
        for (int i = 0; i < longsMaxIdx; i++)
        {
            long mask = 0x1L;
            long xor = longs[i] ^ longs2[i];
            while (mask != 0)
            {
                if ((mask & xor) != 0)
                {
                    return ((mask & longs[i]) != 0) ? 1 : -1;
                }

                mask <<= 1;

            }
        }

        long mask = 0x1L;
        long xor = longs[longsMaxIdx] ^ longs2[longsMaxIdx];
        int i = longsMaxIdx * 64;

        while (i < numBits)
        { 
            if ((mask & xor) != 0)
            {
                return ((mask & longs[longsMaxIdx]) != 0) ? 1 : -1;
            }
            mask <<= 1;
            i++;
        }
        // if the bitsets are equal, then compare based on id:
        return bs1.id == bs2.id ? 0 : bs1.id > bs2.id ? 1 : -1;
    }
}
