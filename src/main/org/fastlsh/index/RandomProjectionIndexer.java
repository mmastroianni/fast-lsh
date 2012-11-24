/*
   Copyright 2012 Michael Mastroianni, Amol Kapile (fastlsh.org)
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

import org.fastlsh.hash.HashFactory;
import org.fastlsh.hash.HashFamily;
import org.fastlsh.util.BitSetWithId;

import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;

public class RandomProjectionIndexer<T> implements Indexer<T>, Closeable
{
	private ObjectOutputStream rawStream;
	private ObjectOutputStream sigStream;
	private int numVectors;
	private Algebra alg;
	private HashFamily family;
	private File directory;
	private IndexOptions options;
	private VectorParser<T> parser;

	public RandomProjectionIndexer(String directory, IndexOptions options) throws IOException {
		this.options = options;
		this.directory = new File(directory);
		
		if (this.directory.exists()) {
			throw new IOException("Output directory exists");
		}
		
		this.directory.mkdir();
		
        rawStream = new ObjectOutputStream(new FileOutputStream(new File(directory, "normalizedVectors")));
        sigStream = new ObjectOutputStream(new FileOutputStream(new File(directory, "signatures")));

        family = new HashFamily(HashFactory.makeProjectionHashFamily(options.vectorDimension, options.numHashes));
        options.hashFamily = family;
        writeOptions();

        alg = new Algebra();
	}
	
	@Override
	public void setParser(VectorParser<T> parser) {
		this.parser = parser;
	}
	
	private void writeOptions() throws IOException {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(directory, "options")));
			out.writeObject(options);
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	@Override
	public void indexVector(T vec) throws IOException {
		indexVector(parser.parse(vec));
	}
    
    public void indexVector(VectorWithId vec) throws IOException {
    	double norm = alg.norm2(vec.vector);
        if(norm == 0.0) return;
        //Compute the signatures non-normalized, but normalize the raw vectors before serialization so that when we check
        // cosine distances, we only have to do dot products
        sigStream.writeObject(new BitSetWithId(vec.id, family.makeSignature(vec.vector)));
        vec.vector.assign(Functions.div(norm));
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
        if(rawStream != null) rawStream.close();
        if(sigStream != null) sigStream.close();
    }
}
