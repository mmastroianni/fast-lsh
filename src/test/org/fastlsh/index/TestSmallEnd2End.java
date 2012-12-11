package org.fastlsh.index;

import java.io.File;

import junit.framework.Assert;

import org.fastlsh.hash.HashFactory;
import org.fastlsh.hash.HashFamily;
import org.fastlsh.parsers.CSVParser;
import org.fastlsh.parsers.VectorParser;
import org.fastlsh.util.LongDoublePair;
import org.junit.Test;

public class TestSmallEnd2End
{
    String input;
    String output      = "/data/fast_lsh/end2endtest";
    int    numHashes   = 128;
    int    numFeatures = 50;
    int    numRows     = 100;

    @Test
    public void test() throws Exception
    {
        IndexUtils.delete(new File(output));       
        File tmp = File.createTempFile("test_vector_data", "dat");
        input = tmp.getAbsolutePath();
        tmp.delete();

        GenerateRandomCSVInputs.generateTestFile(numFeatures, numRows, input);

        IndexOptions options = new IndexOptions();
        options.numHashes = numHashes;
        options.vectorDimension = numFeatures;
        options.hashFamily = new HashFamily(HashFactory.makeProjectionHashFamily(options.vectorDimension, options.numHashes));
        options.numPermutations = 5;

        VectorParser<String> parser = new CSVParser(",");
        IndexUtils.generateSingleThreadedIndex(options, parser, input, output);
        IndexReader idxReader = new IndexReader(output);
        idxReader.initializeSignatures();
        PermutationIndexWriter permWriter = new PermutationIndexWriter(output, idxReader.signatures, options);
        permWriter.createIndex();
        idxReader = null;
        NearestNeighborSearcher searcher = new NearestNeighborSearcher(output);
        int beamWidth = 10;
        double minScore = .7;
        long [] targetIds = new long []{1,2,3,4,5,6,7,8,9};

        for(long id : targetIds)
        {
            LongDoublePair [] sims = searcher.getScoredSimilars(id, beamWidth, options.numPermutations, minScore);
            Assert.assertTrue(containsId(id, sims));
        }

        IndexUtils.delete(new File(input));
    }

    public static boolean containsId(long id, LongDoublePair[] sims)
    {
        for(LongDoublePair p : sims) if(p.l == id) return true;
        return false;
    }

}
