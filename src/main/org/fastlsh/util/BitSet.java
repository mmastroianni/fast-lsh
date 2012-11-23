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

import java.io.Serializable;

/** A simplified and streamlined version of {@link java.util.BitSet}. */
public class BitSet implements Serializable, Cloneable
{
    private static final long serialVersionUID = 744071041169778527L;
    protected final long[]      bits;
    public final int numBits;
    
    public BitSet(BitSet other)
    {
        bits = other.bits.clone();
        numBits = other.numBits;
    }

    public BitSet(int numBits)
    {
        int numLongs = numBits >>> 6;
        if ((numBits & 0x3F) != 0)
        {
            numLongs++;
        }
        bits = new long[numLongs];
        this.numBits = numBits;
    }

    private BitSet(int numBits, long [] bits)
    {
        this.numBits = numBits;
        this.bits = bits;
    }
    
    public boolean get(int index)
    {
        // skipping range check for speed
        return (bits[index >>> 6] & 1L << (index & 0x3F)) != 0L;
    }

    public void set(int index, boolean b)
    {
        if(b) bits[index >>> 6] |= 1L << (index & 0x3F);
        else bits[index >>> 6] &= ~(1L << (index & 0x3F));
    }
    
    public void set(int index)
    {
        // skipping range check for speed
        bits[index >>> 6] |= 1L << (index & 0x3F);
    }

    public void clear(int index)
    {
        // skipping range check for speed
        bits[index >>> 6] &= ~(1L << (index & 0x3F));
    }

    public void clear()
    {
        int length = bits.length;
        for (int i = 0; i < length; i++)
        {
            bits[i] = 0L;
        }
    }

    @Override
    public BitSet clone()
    {
        return new BitSet(numBits, bits.clone());
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder(64 * bits.length);
        for (long l : bits)
        {
            for (int j = 0; j < 64; j++)
            {
                result.append((l & 1L << j) == 0 ? '0' : '1');
            }
            result.append(' ');
        }
        return result.toString();
    }

}
