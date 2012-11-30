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
import java.util.Arrays;
public class BitSetWithId implements Serializable
{
    private static final long serialVersionUID = 1503715050039353843L;
    public final long id;
    public final BitSet bits;
    public BitSetWithId(long iid, BitSet bitset)
    {
        id = iid;
        bits = bitset;
    }
    
    public String toString()
    {
        return "[ id: " + id + " bits: " + bits.toString() + "]";    
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof BitSetWithId))return false;
        BitSetWithId obs = (BitSetWithId)other;
        return id == obs.id? Arrays.equals(bits.bits, obs.bits.bits):false;
    }
}
