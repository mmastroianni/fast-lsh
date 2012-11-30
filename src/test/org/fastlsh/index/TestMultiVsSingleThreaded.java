package org.fastlsh.index;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.fastlsh.hash.HashFactory;
import org.fastlsh.hash.HashFamily;

import org.fastlsh.parsers.CSVParser;
import org.fastlsh.parsers.VectorParser;

import org.fastlsh.util.BitSet;
import org.fastlsh.util.BitSetWithId;
import org.fastlsh.util.LexicographicBitSetComparator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestMultiVsSingleThreaded
{
    // lifted from guava
    public static File createTempDir()
    {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++)
        {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir())
            {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within "
                + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName + "0 to "
                + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }

    private static final int TEMP_DIR_ATTEMPTS = 10000;

    String                   input;
    String                   multiOutput;
    String                   singleOutput;
    int                      numHashes         = 128;
    int                      numFeatures       = 50;
    int                      numRows           = 1000;

    protected void generateSingleThreadedIndex(IndexOptions options,
            VectorParser<String> parser) throws IOException
    {
        BufferedReader reader = null;
        RandomProjectionIndexer<String> indexer = null;

        try
        {
            indexer = new RandomProjectionIndexer<String>(singleOutput, options);
            indexer.setParser(parser);
            reader = new BufferedReader(new FileReader(input));
            String line = "";
            int numLines = 0;
            while ((line = reader.readLine()) != null)
            {
                indexer.indexVector(line.trim());
                numLines++;
            }
            Assert.assertEquals(numRows, numLines);
            
        }
        finally
        {
            if (reader != null)
                reader.close();
            if (indexer != null)
                indexer.close();
        }
    }

    private void generateMultiThreadedIndex(IndexOptions options,
            org.fastlsh.parsers.VectorParser<String> parser) throws Exception
    {
        BufferedReader reader = null;
        ThreadedRandomProjectionIndexer<String> indexer = null;
        try
        {            
            indexer = new ThreadedRandomProjectionIndexer<String>(multiOutput, options, 16, 10000);
            indexer.setParser(parser);
            reader = new BufferedReader(new FileReader(input));
            String line = null;
            while((line = reader.readLine()) != null)
            {
                indexer.indexVector(line.trim());
            }
        }
        finally
        {
            if(reader != null) reader.close();
            if (indexer != null) {
                indexer.close();
            }
        }
    }
    
    protected void delete(String f)
    {
        try
        {
            File tmp = new File(f);
            tmp.delete();
        }
        catch(Exception e){}
        
    }

    @After
    public void tearDown()
    {
        delete(input);
        delete(singleOutput);
        delete(multiOutput);
    }
    
    @Test
    public void test() throws Exception
    {
        File tmp = File.createTempFile("test_vector_data", "dat");
        input = tmp.getAbsolutePath();
        tmp.delete();

        singleOutput = "/data/fast_lsh/sm_test/single";
        multiOutput  = "/data/fast_lsh/sm_test/multi";

        /*
        File singleOutputDir = createTempDir();
        singleOutput = singleOutputDir.getAbsolutePath();
        singleOutputDir.delete();
        
        File multiOutputDir = createTempDir();
        multiOutput = multiOutputDir.getAbsolutePath();
        singleOutputDir.delete();
        */
        
        GenerateRandomCSVInputs.generateTestFile(numFeatures, numRows, input);

        IndexOptions options = new IndexOptions();
        options.numHashes = numHashes;
        options.vectorDimension = numFeatures;        
        options.hashFamily = new HashFamily(HashFactory.makeProjectionHashFamily(options.vectorDimension, options.numHashes));

        VectorParser<String> parser = new CSVParser(",");

        generateSingleThreadedIndex(options, parser);
        generateMultiThreadedIndex(options, parser);
        IndexReader reader1 = new IndexReader(singleOutput);
        reader1.initialize();
        IndexReader reader2 = new IndexReader(multiOutput);
        reader2.initialize();
        
        BitSetWithId [] sigs1 = reader1.signatures;
        BitSetWithId [] sigs2 = reader2.signatures;
        Assert.assertEquals(numRows, sigs1.length);        
        Assert.assertEquals(sigs1.length, sigs2.length);
        Assert.assertTrue(areSame(sigs1, sigs2, reader2.sigMap));
        
        Assert.assertEquals(numRows, reader1.rawVectorMap.size());
        Assert.assertTrue(areSame(reader1.rawVectorMap, reader2.rawVectorMap));
    }
    
    private boolean areSame(BitSetWithId[] sigs1, BitSetWithId[] sigs2, TLongObjectHashMap<BitSet> sigMap)
    {
        Comparator<BitSetWithId> comp = new LexicographicBitSetComparator();
        Arrays.sort(sigs1, comp);
        Arrays.sort(sigs2, comp);
        for(int i = 0, m = sigs1.length; i < m; i++)
        {
            long id = sigs1[i].id;
            BitSet bs2 = sigMap.get(id);
            Assert.assertArrayEquals(sigs1[i].bits.bits, bs2.bits);
        }
        return true;
    }

    private boolean areSame(TLongObjectHashMap<double[]> map1,
                            TLongObjectHashMap<double[]> map2)
    {
        if(map1.size() != map2.size()) return false;
        TLongObjectIterator<double []> iter = map1.iterator();
        while(iter.hasNext())
        {
            iter.advance();
            if(!Arrays.equals(iter.value(), map2.get(iter.key()))) return false;
        }
        return true;
    }
}
