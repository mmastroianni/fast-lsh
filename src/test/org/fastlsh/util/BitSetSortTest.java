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

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;

import org.fastlsh.util.BitSet;
import org.fastlsh.util.Signature;
import org.fastlsh.util.LexicographicBitSetComparator;
import org.junit.Test;

public class BitSetSortTest
{
    public static boolean isLessThanUnsigned(long n1, long n2) {
        boolean comp = (n1 < n2);
        if ((n1 < 0) != (n2 < 0)) {
          comp = !comp;
        }
        return comp;
      }
    
    @Test 
    public void stuff()
    {
        BitSet b1 = new BitSet(64);
        b1.set(0);          
        BitSet b2 = new BitSet(64);
        b2.set(1);        
        BitSet b3 = new BitSet(64);
        b3.set(2);        
        BitSet b4 = new BitSet(64);
        b4.set(0);          
        b4.set(63);          
        long l1 = b1.bits[0];
        long l2 = b2.bits[0];
        long l3 = b3.bits[0];
        long l4 = b4.bits[0];

        
        Assert.assertTrue(isLessThanUnsigned(l1, l4));
        Assert.assertFalse(isLessThanUnsigned(l4, l1));
        Assert.assertTrue(isLessThanUnsigned(l1, l3));
        Assert.assertTrue(isLessThanUnsigned(l2, l3));
        Assert.assertTrue(isLessThanUnsigned(l3, l4));

    }

    @Test
    public void duffTest()
    {
        BitSet b1 = new BitSet(64);
        b1.set(0);          
        BitSet b2 = new BitSet(64);
        b2.set(1);        
        BitSet b3 = new BitSet(64);
        b3.set(2);        
        BitSet b4 = new BitSet(64);
        b4.set(3);
        BitSet b5 = new BitSet(64);
        b5.set(0);          
        b5.set(63);          

        ArrayList<Signature> al = new ArrayList<Signature>();
        al.add(new Signature(1,b1));
        al.add(new Signature(2,b2));
        al.add(new Signature(4,b4));
        al.add(new Signature(3,b3));
        al.add(new Signature(5,b5));
        
        Collections.sort(al, new LexicographicBitSetComparator());
        Assert.assertEquals(4, al.get(0).id);
        Assert.assertEquals(3, al.get(1).id);
        Assert.assertEquals(2, al.get(2).id);
        Assert.assertEquals(1, al.get(3).id);
        Assert.assertEquals(5, al.get(4).id);
        
    }

    
    @Test
    public void sortSmall()
    {
        BitSet b1 = new BitSet(4);
        b1.set(3);  
        b1.set(2);  
        b1.set(1);  
        b1.set(0);  
        
        BitSet b2 = new BitSet(4);
        BitSet b3 = new BitSet(4);
        b3.set(2);
        
        BitSet b4 = new BitSet(4);
        b4.set(2);
        b4.set(3);

        ArrayList<Signature> al = new ArrayList<Signature>();
        al.add(new Signature(1,b1));
        al.add(new Signature(2,b2));
        al.add(new Signature(4,b4));
        al.add(new Signature(3,b3));
        
        Collections.sort(al, new LexicographicBitSetComparator());
        Assert.assertEquals(2, al.get(0).id);
        Assert.assertEquals(3, al.get(1).id);
        Assert.assertEquals(4, al.get(2).id);
        Assert.assertEquals(1, al.get(3).id);
        
    }

    @Test
    public void sortBigger()
    {
        BitSet b11 = new BitSet(356);
        b11.set(3);  
        b11.set(2);  
        b11.set(1);  
        b11.set(0);  
        b11.set(200);  

        BitSet b1 = new BitSet(356);
        b1.set(3);  
        b1.set(2);  
        b1.set(1);  
        b1.set(0);  
        
        BitSet b2 = new BitSet(356);
        BitSet b3 = new BitSet(356);
        b3.set(2);
        
        BitSet b4 = new BitSet(356);
        b4.set(2);
        b4.set(3);

        ArrayList<Signature> al = new ArrayList<Signature>();
        al.add(new Signature(11,b11));
        al.add(new Signature(1,b1));
        al.add(new Signature(2,b2));
        al.add(new Signature(4,b4));
        al.add(new Signature(3,b3));
        
        Collections.sort(al, new LexicographicBitSetComparator());
        Assert.assertEquals(2, al.get(0).id);
        Assert.assertEquals(3, al.get(1).id);
        Assert.assertEquals(4, al.get(2).id);
        Assert.assertEquals(1, al.get(3).id);
        Assert.assertEquals(11, al.get(4).id);
        
    }

}
