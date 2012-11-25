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
import org.apache.commons.cli.ParseException;
import org.fastlsh.hash.HashFactory;
import org.fastlsh.hash.HashFamily;
import org.fastlsh.util.BlockingThreadPool;
import org.fastlsh.util.RequiredOption;
import org.fastlsh.util.ResourcePool;
import org.fastlsh.util.SimpleCli;

import cern.colt.matrix.linalg.Algebra;

public class ThreadedRandomProjectionIndexer
{
    public ThreadedRandomProjectionIndexer()
    {
        
    }
    
    static String defaultThreads = "10";
    static String sigHead = "signature_";
    static String vecHead = "vector_";
    
    protected static ResourcePool<ObjectOutputStream> allocateWriters(String dirName, String fileNameHead, int numWriters) throws FileNotFoundException, IOException
    {
        File dir = new File(dirName);
        dir.mkdir();
        ResourcePool<ObjectOutputStream> p = new ResourcePool<ObjectOutputStream>();
        for(int i = 0; i < numWriters; i++)
        {
            p.add(new ObjectOutputStream(new FileOutputStream(new File(dir, fileNameHead + i))));
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
    
    public static void main(String [] args) throws ParseException, IOException, InterruptedException
    {
        CommandLine cmd = new SimpleCli()
        .addOption(new RequiredOption("i", true, "text file containing .csv of input data"))
        .addOption(new RequiredOption("or", true, "output directory containing serialized colt vectors for raw parsed data"))
        .addOption(new RequiredOption("os", true, "output directory containing serialized bitset signatures for raw parsed data"))
        .addOption(new RequiredOption("d", true, "dimension of vectors"))
        .addOption(new RequiredOption("sep", true, "separator character delimiting fields in input"))        
        .addOption(new RequiredOption("b", true, "batch size: number of lines to push into thread jobs"))
        .addOption(new RequiredOption("n", true, "number of hashes in hash family"))
        .addOption(new Option("t", true, "number of Threads"))
        .parse(args);
        
        int numHashes = Integer.parseInt(cmd.getOptionValue("n"));
        int vecLen = Integer.parseInt(cmd.getOptionValue("d"));
        final HashFamily family = new HashFamily(HashFactory.makeProjectionHashFamily(vecLen, numHashes));
        final VectorParser parser = new CSVParser(cmd.getOptionValue("sep"));
        BufferedReader reader = null;
        final int batchSize = Integer.parseInt(cmd.getOptionValue("b"));
        int numThreads = Integer.parseInt(cmd.getOptionValue("t", defaultThreads));
        long start = System.currentTimeMillis();
        final ResourcePool<ObjectOutputStream> vecWriters = allocateWriters(cmd.getOptionValue("or"), vecHead, numThreads);
        final ResourcePool<ObjectOutputStream> sigWriters = allocateWriters(cmd.getOptionValue("os"), sigHead, numThreads);
        
        final BlockingThreadPool pool = new BlockingThreadPool(numThreads, numThreads);

        int numLines = 0;
        try
        {            
            reader = new BufferedReader(new FileReader(cmd.getOptionValue("i")));
            final Algebra alg = new Algebra();
            String line = "";
            List<String> curList = new ArrayList<String>();
            while((line = reader.readLine()) != null)
            {
                curList.add(line.trim());
                if(curList.size() == batchSize)
                {
                    pool.execute(new IndexerTask(vecWriters, sigWriters, alg, curList, parser,family));
                    curList = new ArrayList<String>();
                }
            }
            if(curList.size() != 0) pool.execute(new IndexerTask(vecWriters, sigWriters, alg, curList, parser,family));

            
        }
        finally
        {
            if(reader != null) reader.close();
        }
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        closeHandles(vecWriters);
        closeHandles(sigWriters);
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time in seconds: " + ((end -start)/1000));
        System.out.println("Total items: " + numLines);
        System.out.println("input dimensions: " + vecLen);
        System.out.println("number of hashes: " + numHashes);
    }
}
