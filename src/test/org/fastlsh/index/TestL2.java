package org.fastlsh.index;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

public class TestL2
{
    String input;
    String output      = "/home/akapila/Desktop/lsh/l2";
    int    numHashes   = 128;
    int    numFeatures = 50;
    int    numRows     = 10000;

    @Test
    public void test() throws Exception
    {
        IndexUtils.delete(new File(output));
        File tmp = File.createTempFile("test_vector_data", "dat");
        input = tmp.getAbsolutePath();
//        tmp.delete();

        GenerateRandomCSVInputs.generateTestFile(numFeatures, numRows, input);

        IndexOptions options = new IndexOptions();
        options.numHashes = numHashes;
        options.vectorDimension = numFeatures;
        options.hashFamily = HashFamily.getL2HashFamily(options.vectorDimension, options.numHashes);
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
        double maxDistance = 100;
        long [] targetIds = new long []{1,2,3,4,5,6,7,8,9};

        Comparator<Neighbor> comparator = new Neighbor.DissimilarityComparator();
        ScoreThreshold thresh = new L2Threshold(maxDistance);
        TLongObjectHashMap<Neighbor []> allSims = new TLongObjectHashMap<Neighbor []>();
        for(long id : targetIds)
        {
            Neighbor [] sims = searcher.getScoredNeighbors(id, beamWidth, options.numPermutations, 20, comparator, thresh);
            if(sims != null) allSims.put(id, sims);
            Assert.assertTrue(containsId(id, sims));
        }

//        IndexUtils.delete(new File(input));
        System.out.println("input: " + input);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output, "results.txt")));
        TLongObjectIterator<Neighbor []> ids = allSims.iterator();
        while(ids.hasNext()) {
        	ids.advance();
        	long id = ids.key();
        	writer.write(id + "\n--------------\n");
        	Neighbor [] sims = allSims.get(id);
        	for(Neighbor similar : sims) writer.write(similar.id + "," + similar.score + "\n");
        	writer.write("\n\n");
        }
        writer.flush();
        writer.close();
    }

    public static boolean containsId(long id, Neighbor[] sims)
    {
        for(Neighbor p : sims) if(p.id == id) return true;
        return false;
    }

}
