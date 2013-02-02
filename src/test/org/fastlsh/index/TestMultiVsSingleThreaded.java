package org.fastlsh.index;

import java.io.File;
import java.io.IOException;

import org.fastlsh.hash.HashFamily;

import org.fastlsh.parsers.CSVParser;
import org.fastlsh.parsers.VectorParser;

import org.fastlsh.util.Signature;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestMultiVsSingleThreaded
{

    String                   input;
    String                   singleOutput      = "/data/fast_lsh/sm_test/single";
    String                   multiOutput       = "/data/fast_lsh/sm_test/multi";

    int                      numHashes         = 128;
    int                      numFeatures       = 50;
    int                      numRows           = 1000;

    protected void generateSingleThreadedIndex(IndexOptions options,
            VectorParser<String> parser) throws IOException
    {
        Assert.assertEquals(numRows, IndexUtils.generateSingleThreadedIndex(options, parser, input, singleOutput));
    }

    private void generateMultiThreadedIndex(IndexOptions options,
            org.fastlsh.parsers.VectorParser<String> parser) throws Exception
    {
        IndexUtils.generateMultiThreadedIndex(options, parser, input, multiOutput);
    }


    @After
    public void tearDown()
    {
        IndexUtils.delete(input);
        IndexUtils.delete(singleOutput);
        IndexUtils.delete(multiOutput);
    }

    @Test
    public void test() throws Exception
    {
        File tmp = File.createTempFile("test_vector_data", "dat");
        input = tmp.getAbsolutePath();
        tmp.delete();

        /*
        File singleOutputDir = createTempDir();
        singleOutput = singleOutputDir.getAbsolutePath();
        singleOutputDir.delete();

        File multiOutputDir = createTempDir();
        multiOutput = multiOutputDir.getAbsolutePath();
        multiOutputDir.delete();
        */
        GenerateRandomCSVInputs.generateTestFile(numFeatures, numRows, input);

        IndexOptions options = new IndexOptions();
        options.numHashes = numHashes;
        options.vectorDimension = numFeatures;
        options.hashFamily = HashFamily.getCosineHashFamily(options.vectorDimension, options.numHashes);

        VectorParser<String> parser = new CSVParser(",");

        generateSingleThreadedIndex(options, parser);
        generateMultiThreadedIndex(options, parser);
        IndexReader reader1 = new IndexReader(singleOutput);
        reader1.initialize();
        reader1.initializeSignatures();
        reader1.initializeRawVecs();
        IndexReader reader2 = new IndexReader(multiOutput);
        reader2.initialize();
        reader2.initializeSignatures();
        reader2.initializeRawVecs();

        Signature[] sigs1 = reader1.signatures;
        Signature[] sigs2 = reader2.signatures;
        Assert.assertEquals(numRows, sigs1.length);
        Assert.assertEquals(sigs1.length, sigs2.length);
        Assert.assertTrue(IndexUtils.areSame(sigs1, sigs2));

        Assert.assertEquals(numRows, reader1.rawVectorMap.size());
        Assert.assertTrue(IndexUtils.areSame(reader1.rawVectorMap, reader2.rawVectorMap));
    }

}
