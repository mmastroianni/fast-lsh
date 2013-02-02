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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.fastlsh.hash.HashFamily;
import org.fastlsh.util.Signature;


public class RandomProjectionSignatureIndexWriter<T> extends SignatureIndexWriter<T> implements Closeable
{
	private ObjectOutputStream rawStream;
	private ObjectOutputStream sigStream;
	private int numVectors;
	private HashFamily family;

	public RandomProjectionSignatureIndexWriter(String directory, IndexOptions options) throws IOException {
	    super(directory, options);
        family = options.hashFamily;
		rawStream = new ObjectOutputStream(new FileOutputStream(new File(directory, Constants.inputData)));
        sigStream = new ObjectOutputStream(new FileOutputStream(new File(directory, Constants.signatures)));
	}
		
	@Override
	public void indexVector(T vec) throws IOException {
		indexVector(parser.parse(vec));
	}
    
    public void indexVector(VectorWithId vec) throws IOException {
    	double norm = vec.norm2();
        if(norm == 0.0) return;  // TODO: create a separate zeros file for these.
        sigStream.writeObject(new Signature(vec.id, family.makeSignature(vec)));
        rawStream.writeObject(vec);
        numVectors++;
        if(numVectors%10000 == 0)
        {
            rawStream.flush();
            sigStream.flush();
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
    }
}
