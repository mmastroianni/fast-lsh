package org.fastlsh.index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import org.fastlsh.util.OutputAlreadyExistsException;
import org.fastlsh.util.RequiredOption;
import org.fastlsh.util.SimpleCli;


import org.fastlsh.hash.HashFamily;
import org.fastlsh.parsers.CSVParser;
import org.fastlsh.parsers.VectorParser;

/**
 * Main entry point for creating an approximate nearest neighbor index given a .csv file containing a (dense) representation of input vectors
 * Calling the main function of this class with the appropriate arguments will create a searchable index, which can be read usoiin the IndexReader class,
 * and searched using the NearestNeighborSearcher class
 */
public class CSVIndexer {

    /**
     * @param args specified at the commandline with -x syntax
     * <br> -i input file containing delimited (dense) text representation of input vectors
     * <br>-o output directory: this directory should not already exist: the application will try to create it, and throw if it already exists, in order to make it]
     * harder to overwrite past work.
     * <br>-d dimension of input vectors
     * <br>-sep separator used in input file (typically comma or tab)
     * <br>-np number of permutations to create for searching
     * <br>-n number of hashes in hash family (number of bits in lsh signature)
     * 
     * @throws ParseException
     * @throws IOException
     * @throws InvalidIndexException
     * @throws OutputAlreadyExistsException
     */
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
        options.hashFamily = HashFamily.getCosineHashFamily(options.vectorDimension, options.numHashes);
        options.numPermutations = Integer.parseInt(cmd.getOptionValue("np"));
        VectorParser<String> parser = new CSVParser(cmd.getOptionValue("sep"));
        
        BufferedReader reader = null;
        RandomProjectionSignatureIndexWriter<String> indexer = null;
        int numLines = 0;
        long start = System.currentTimeMillis();
        try
        {
        	indexer = new RandomProjectionSignatureIndexWriter<String>(cmd.getOptionValue("o"), options);
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
        PermutationIndexWriter permWriter = new PermutationIndexWriter(indexer.rootDirName, idxReader.signatures, options);
        permWriter.createIndex();

        long end = System.currentTimeMillis();
        System.out.println("Elapsed time in seconds: " + ((end -start)/1000));
        System.out.println("Total items: " + numLines);
        System.out.println("input dimensions: " + options.vectorDimension);
        System.out.println("number of hashes: " + options.vectorDimension);
    }
}
