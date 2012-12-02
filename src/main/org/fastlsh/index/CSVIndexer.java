package org.fastlsh.index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import org.fastlsh.util.OutputAlreadyExistsException;
import org.fastlsh.util.RequiredOption;
import org.fastlsh.util.SimpleCli;


import org.fastlsh.hash.HashFactory;
import org.fastlsh.hash.HashFamily;
import org.fastlsh.parsers.CSVParser;
import org.fastlsh.parsers.VectorParser;

public class CSVIndexer {

    public static void main(String [] args) throws ParseException, IOException, InvalidIndexException, OutputAlreadyExistsException
    {
        CommandLine cmd = new SimpleCli()
        .addOption(new RequiredOption("i", true, "text file containing .csv of input data"))
        .addOption(new RequiredOption("o", true, "output directory"))
        .addOption(new RequiredOption("d", true, "dimension of vectors"))
        .addOption(new RequiredOption("sep", true, "separator character delimiting fields in input"))        
        .addOption(new RequiredOption("np", true, "number of permutations to create for searching"))        
        .addOption(new RequiredOption("n", true, "number of hashes in hash family")).parse(args);
        
        IndexOptions options = new IndexOptions();
        options.numHashes = Integer.parseInt(cmd.getOptionValue("n"));
        options.vectorDimension = Integer.parseInt(cmd.getOptionValue("d"));
        options.hashFamily = new HashFamily(HashFactory.makeProjectionHashFamily(options.vectorDimension, options.numHashes));
        options.numPermutations = Integer.parseInt(cmd.getOptionValue("np"));
        VectorParser<String> parser = new CSVParser(cmd.getOptionValue("sep"));
        
        BufferedReader reader = null;
        RandomProjectionIndexer<String> indexer = null;
        int numLines = 0;
        long start = System.currentTimeMillis();
        try
        {
        	indexer = new RandomProjectionIndexer<String>(cmd.getOptionValue("o"), options);
        	indexer.setParser(parser);
            reader = new BufferedReader(new FileReader(cmd.getOptionValue("i")));
            String line = "";
            while((line = reader.readLine()) != null)
            {
                indexer.indexVector(line.trim());
            }            
        }
        finally
        {
            if(reader != null) reader.close();
            if (indexer != null) indexer.close();
        }

        IndexReader idxReader = new IndexReader(indexer.rootDirName);
        idxReader.initializeSignatures();
        PermutationIndexWriter permWriter = new PermutationIndexWriter(indexer.rootDirName, idxReader.signatures, 42, options);
        permWriter.createIndex();

        long end = System.currentTimeMillis();
        System.out.println("Elapsed time in seconds: " + ((end -start)/1000));
        System.out.println("Total items: " + numLines);
        System.out.println("input dimensions: " + options.vectorDimension);
        System.out.println("number of hashes: " + options.vectorDimension);
    }
}
