package org.fastlsh.index;

import java.io.File;
import java.util.Comparator;

import junit.framework.Assert;

import org.fastlsh.hash.HashFamily;
import org.fastlsh.parsers.CSVParser;
import org.fastlsh.parsers.VectorParser;
import org.fastlsh.query.NearestNeighborSearcher;
import org.fastlsh.threshold.L2Threshold;
import org.fastlsh.threshold.ScoreThreshold;
import org.fastlsh.util.Neighbor;
import org.junit.Test;

public class TestSmallEnd2End
{
    String input;
    String output      = "/home/akapila/Desktop/lsh/cosine";
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
        options.hashFamily = HashFamily.getCosineHashFamily(options.vectorDimension, options.numHashes);
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

        Comparator<Neighbor> comparator = new Neighbor.DissimilarityComparator();
        ScoreThreshold thresh = new L2Threshold(minScore);
        for(long id : targetIds)
        {
            Neighbor [] sims = searcher.getScoredNeighbors(id, beamWidth, options.numPermutations, -1, comparator, thresh);
            Assert.assertTrue(containsId(id, sims));
        }

        IndexUtils.delete(new File(input));
    }

    public static boolean containsId(long id, Neighbor[] sims)
    {
        for(Neighbor p : sims) if(p.id == id) return true;
        return false;
    }

}
