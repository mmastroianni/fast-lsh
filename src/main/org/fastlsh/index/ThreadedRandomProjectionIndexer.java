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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.fastlsh.hash.HashFactory;
import org.fastlsh.hash.HashFamily;
import org.fastlsh.util.BlockingThreadPool;
import org.fastlsh.util.RequiredOption;
import org.fastlsh.util.ResourcePool;
import org.fastlsh.util.SimpleCli;

import cern.colt.matrix.linalg.Algebra;

public class ThreadedRandomProjectionIndexer<T> implements Indexer<T>, Closeable
{   
    static String defaultThreads = "10";
    static String sigHead = "signature_";
    static String vecHead = "vector_";
    
	private List<T> curList;
	private BlockingThreadPool pool;
	private Algebra alg;
	private ResourcePool<ObjectOutputStream> vecWriters;
	private ResourcePool<ObjectOutputStream> sigWriters;
	private VectorParser<T> parser;
    private int batchSize;
    private HashFamily family;

	public ThreadedRandomProjectionIndexer(String directory, IndexOptions options, int numThreads, int batchSize) throws IOException
    {
    	this.batchSize = batchSize;
        curList = new ArrayList<T>();

        pool = new BlockingThreadPool(numThreads, numThreads);
        alg = new Algebra();

        vecWriters = allocateWriters(new File(directory, "normalizedVectors"), vecHead, numThreads);
        vecWriters.open();
        sigWriters = allocateWriters(new File(directory, "signatures"), sigHead, numThreads);
        sigWriters.open();
        
        family = new HashFamily(HashFactory.makeProjectionHashFamily(options.vectorDimension, options.numHashes));
        options.hashFamily = family;
    }
    
    protected static ResourcePool<ObjectOutputStream> allocateWriters(File directory, String fileNameHead, int numWriters) throws FileNotFoundException, IOException
    {
        directory.mkdirs();
        ResourcePool<ObjectOutputStream> p = new ResourcePool<ObjectOutputStream>();
        for(int i = 0; i < numWriters; i++)
        {
            p.add(new ObjectOutputStream(new FileOutputStream(new File(directory, fileNameHead + i))));
        }
        p.open();
        return p;
    }

    protected static void closeHandles(ResourcePool<ObjectOutputStream> pool) throws InterruptedException, IOException
    {
        do
        {
            ObjectOutputStream oos = pool.acquire();
            oos.close();
            pool.removeNow(oos);
        }
        while(pool.haveAvailable());
        pool.close();
    }
    
    public static void main(String [] args) throws Exception
    {
        CommandLine cmd = new SimpleCli()
        .addOption(new RequiredOption("i", true, "text file containing .csv of input data"))
        .addOption(new RequiredOption("o", true, "output directory"))
        .addOption(new RequiredOption("d", true, "dimension of vectors"))
        .addOption(new RequiredOption("sep", true, "separator character delimiting fields in input"))        
        .addOption(new RequiredOption("b", true, "batch size: number of lines to push into thread jobs"))
        .addOption(new RequiredOption("n", true, "number of hashes in hash family"))
        .addOption(new Option("t", true, "number of Threads"))
        .parse(args);
        
        IndexOptions options = new IndexOptions();
        options.numHashes = Integer.parseInt(cmd.getOptionValue("n"));
        options.vectorDimension = Integer.parseInt(cmd.getOptionValue("d"));
        BufferedReader reader = null;
        int batchSize = Integer.parseInt(cmd.getOptionValue("b"));
        int numThreads = Integer.parseInt(cmd.getOptionValue("t", defaultThreads));
        long start = System.currentTimeMillis();
        
        ThreadedRandomProjectionIndexer<String> indexer = null;
        
        int numLines = 0;
        try
        {            
        	indexer = new ThreadedRandomProjectionIndexer<String>(cmd.getOptionValue("o"), options, numThreads, batchSize);
        	indexer.setParser(new CSVParser(cmd.getOptionValue("sep")));
            reader = new BufferedReader(new FileReader(cmd.getOptionValue("i")));
            String line = null;
            while((line = reader.readLine()) != null)
            {
                indexer.indexVector(line.trim());
                if (numLines++ % 10000 == 0) {
                	System.out.println(numLines);
                }
            }

        }
        finally
        {
            if(reader != null) reader.close();
            if (indexer != null) {
            	indexer.close();
            }
        }
        
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time in seconds: " + ((end -start)/1000));
        System.out.println("Total items: " + numLines);
        System.out.println("input dimensions: " + options.vectorDimension);
        System.out.println("number of hashes: " + options.numHashes);
    }
   

	@Override
	public void setParser(VectorParser<T> parser) {
		this.parser = parser;
	}

	@Override
	public void indexVector(T vector) throws Exception {
		curList.add(vector);
        if(curList.size() == batchSize)
        {
            pool.execute(new IndexerTask<T>(vecWriters, sigWriters, alg, curList, parser, family));
            curList = new ArrayList<T>();
        }
	}
	
	public void close() throws IOException {
		try {
	    	if(curList.size() != 0) pool.execute(new IndexerTask<T>(vecWriters, sigWriters, alg, curList, parser, family));

	    	pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

			closeHandles(vecWriters);
			closeHandles(sigWriters);
		}
		catch (InterruptedException ex) {
			// FIXME keep waiting?
			throw new IOException(ex);
		}
	}
}
