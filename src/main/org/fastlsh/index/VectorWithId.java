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

import java.io.Serializable;


public class VectorWithId implements Serializable
{
    private static final long serialVersionUID = 9163123870965523774L;
    final protected long id;
    //final protected DoubleMatrix1D vector;
    final protected double [] vals;
    public VectorWithId(long iid,  double [] vector)
    {
        id = iid;
        vals = vector;
    }
    
    public double norm2()
    {
        double out = 0;
        for(int i = 0, m = vals.length; i < m; i++)
        {
            out += vals[i]*vals[i];
        }
        return Math.sqrt(out);
    }
    
    public double dotProduct(VectorWithId other)
    {
        return dotProduct(vals, other.vals);
    }

    public double dotProduct(double [] o)
    {
        return dotProduct(vals, o);
    }

    public static double dotProduct(double [] t, double [] o)
    {
        assert(o.length == t.length);
        double out= 0;
        for(int i = 0, m = t.length; i < m; i++)
        {
            out += t[i]*o[i];
        }
        return out;        
    }
    
    public void scalarDivide(double s)
    {
        for(int i = 0, m = vals.length; i < m; i++)
        {
            vals[i] /= s;
        }
    }
    
}
