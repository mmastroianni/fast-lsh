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

import java.io.ObjectOutputStream;
import java.util.List;

import org.fastlsh.hash.HashFamily;
import org.fastlsh.util.BitSetWithId;
import org.fastlsh.util.ResourcePool;


public class IndexerTask<T> implements Runnable
{
    List<T> inputs;
    ResourcePool<ObjectOutputStream> vecWriters;
    ResourcePool<ObjectOutputStream> sigWriters;
    VectorParser<T> parser;
    HashFamily family;
    
    public IndexerTask()
    {
        
    }
    
    public IndexerTask(ResourcePool<ObjectOutputStream> vecWriters,
                       ResourcePool<ObjectOutputStream> sigWriters, 
                       List<T> curList, 
                       VectorParser<T> parser,
                       HashFamily family)
    {
        this.vecWriters = vecWriters;
        this.sigWriters = sigWriters;
        inputs = curList;
        this.parser = parser;
        this.family = family;
    }
    
    @Override
    public void run()
    {
        ObjectOutputStream sigStream = null;
        ObjectOutputStream vecStream = null;
        try
        {
            sigStream = sigWriters.acquire();
            vecStream = vecWriters.acquire();
            for(T line : inputs)
            {
                VectorWithId vec = parser.parse(line);
                double norm = vec.norm2();
                if(norm == 0.0) continue;
                // Compute the signatures non-normalized, but normalize the raw vectors before serialization so that when we check
                // cosine distances, we only have to do dot products
                sigStream.writeObject(new BitSetWithId(vec.id, family.makeSignature(vec)));
                vec.scalarDivide(norm);
                vecStream.writeObject(vec);
            }
            sigStream.reset();                    
            vecStream.reset();                                
        }
        catch(Exception e)
        {
            throw(new RuntimeException(e));
        }
        finally
        {
            if(sigStream != null) sigWriters.release(sigStream);
            if(vecStream != null) vecWriters.release(vecStream);
        }
    }

}
