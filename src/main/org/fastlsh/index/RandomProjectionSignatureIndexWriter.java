/*
   Copyright 2012 Michael Mastroianni, Amol Kapila (fastlsh.org)
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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.fastlsh.hash.HashFamily;
import org.fastlsh.util.BitSet;
import org.fastlsh.util.Signature;


public class RandomProjectionSignatureIndexWriter<T> extends SignatureIndexWriter<T> implements Closeable
{
	private ObjectOutputStream rawStream;
	private ObjectOutputStream sigStream;
	private BufferedWriter textWriter;
	private int numVectors;
	private HashFamily family;

	public RandomProjectionSignatureIndexWriter(String directory, IndexOptions options) throws IOException {
	    super(directory, options);
        family = options.hashFamily;
		rawStream = new ObjectOutputStream(new FileOutputStream(new File(directory, Constants.normalizedVectors)));
        sigStream = new ObjectOutputStream(new FileOutputStream(new File(directory, Constants.signatures)));
        textWriter = new BufferedWriter(new FileWriter(new File(directory, "sigs.txt")));
	}
		
	@Override
	public void indexVector(T vec) throws IOException {
		indexVector(parser.parse(vec));
	}
    
    public void indexVector(VectorWithId vec) throws IOException {
    	double norm = vec.norm2();
        if(norm == 0.0) return;
        //Compute the signatures non-normalized, but normalize the raw vectors before serialization so that when we check
        // cosine distances, we only have to do dot products
        sigStream.writeObject(new Signature(vec.id, family.makeSignature(vec)));
//        vec.scalarDivide(norm);
        rawStream.writeObject(vec);
        textWriter.write(vec.id + ",");
        BitSet bs = family.makeSignature(vec);
        for (int i = 0, max = 127; i < max; i++) {
        	textWriter.write((bs.get(i) ? "1" : "0"));
        }
        textWriter.write("\n");
        numVectors++;
        if(numVectors%10000 == 0)
        {
            rawStream.flush();
            sigStream.flush();
            textWriter.flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        if(rawStream != null) {
        	rawStream.flush();
        	rawStream.close();
        }
        if(sigStream != null) {
        	sigStream.flush();
        	sigStream.close();
        }
        if(textWriter != null) {
        	textWriter.flush();
        	textWriter.close();
        }
    }
}
