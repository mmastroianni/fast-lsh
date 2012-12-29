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

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.cli.CommandLine;
import org.fastlsh.threshold.CosineThreshold;
import org.fastlsh.threshold.L2Threshold;
import org.fastlsh.threshold.ScoreThreshold;
import org.fastlsh.util.Neighbor;
import org.fastlsh.util.LongStoreReader;
import org.fastlsh.util.MathFns;
import org.fastlsh.util.RequiredOption;
import org.fastlsh.util.SimpleCli;

/**
 * Class for doing approximate nearest neighbor search on and index built with one of the indexer classes. 
 * Provides facilities for getting a list of (potentially) similar items straight from the permutation lists, as well as filtering them via a 
 * min similarity using cosine distance on the raw vectors.
 * 
 * <br>For an example of use of this class, see the test in 
 * org.fastlsh.index.TestSmallEnd2End in the test source tree
 *
 */
public class NearestNeighborSearcher
{
    IndexReader reader;
    TLongObjectHashMap<double []> rawVectorMap;
    LongStoreReader [] permutationLists;
    int maxPermutations;
    
    /**
     * Create a searcher based on the output directory of one of our indexers
     * @param indexDir
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidIndexException
     */
    public NearestNeighborSearcher(String indexDir) throws FileNotFoundException, IOException, ClassNotFoundException, InvalidIndexException
    {
        reader = new IndexReader(indexDir);
        reader.initializeOptions();
        reader.initializePermutationIndex();
        reader.initializePermutationLists();
        reader.initializeRawVecs();
        rawVectorMap = reader.rawVectorMap;
        maxPermutations = reader.options.numPermutations;
        permutationLists = reader.permutationLists;
    }
    
    /**
     * Get all similar items within a particular distance in each of the first numPermutations permutations list. You can tune the number returned by altering either the beamwidth
     * or the number of permutations used. Note that numPermutatiosn must be <= the number of permutation lists written out by the indexer: this class does not create new permutation lists
     * @param id id of object to search for
     * @param beamRadius max number greater and lesser than this object in each of the ordered permutation lists
     * @param numPermutations number of permutations to use
     * @return
     * @throws InvalidIndexException
     * @throws IOException
     */
    public long [] getSimilars(long id, int beamRadius, int numPermutations) throws InvalidIndexException, IOException
    {
        if(numPermutations > maxPermutations) throw(new InvalidIndexException(reader.rootDir, "Max  available permutations is: " + maxPermutations + ". " + numPermutations + " were requested"));
        TLongHashSet sims = new TLongHashSet();
        if(!reader.permutationIndex.containsKey(id)) return null;
        int [] positions = reader.permutationIndex.get(id);
        if(maxPermutations != positions.length) throw(new InvalidIndexException(reader.rootDir, "Found invalid number of permutations: " + positions.length+ " for input id: " + id));
        for(int i = 0; i < numPermutations; i++)
        {
            getSimilars(positions[i], beamRadius, permutationLists[i], sims);
        }        
        return sims.toArray();
    }
    
    /**
     * Helper method for getSimilars(long id, int beamRadius, int numPermutations). 
     * @param pos
     * @param beamRadius
     * @param r
     * @param output
     * @throws IOException
     */
    private void getSimilars(long pos, int beamRadius, LongStoreReader r, TLongHashSet output) throws IOException
    {
        long max = Math.min(r.length(), (long)pos+beamRadius);
        long min = Math.max(0, (long)pos-beamRadius);
        long [] ids = r.get(min, max);
        output.addAll(ids);
    }
    
    /**
     * Get list of similar items for input within beamRadius in first numPermutations permutation lists which have a similarity score 
     * >= minScore 
     * @param srcId id of object to search for
     * @param beamRadius max number greater and lesser than this object in each of the ordered permutation lists
     * @param numPermutations number of permutations to use
     * @param threshold similarity threshold
     * @return
     * @throws InvalidIndexException
     * @throws IOException
     */
    public Neighbor [] getScoredSimilars(long srcId, int beamRadius, int numPermutations, Comparator<Neighbor> comparator, ScoreThreshold scoreThresh) throws InvalidIndexException, IOException
    {
    	long [] inputs = getSimilars(srcId, beamRadius, numPermutations);
        double [] srcVec = rawVectorMap.get(srcId);
        if(srcVec == null) return null;
        ArrayList<Neighbor> tmp = new ArrayList<Neighbor>();
        for(int i = 0, max = inputs.length; i < max; i++) {
            long targetId = inputs[i];
            double [] targetVec = rawVectorMap.get(targetId);
            double score = scoreThresh.score(srcVec, targetVec);
            if (scoreThresh.threshold(score)) tmp.add(new Neighbor(targetId, score));
        }
        
        Neighbor [] retval = tmp.toArray(new Neighbor[tmp.size()]);
        if (retval != null) Arrays.sort(retval, comparator);
        return retval;
    }

    /**
     * Helper method for batch file usage: read a list of object ids to search for near neighbors for, one per line, from an input text file
     * @param inputFile text file containing ids, one per line
     * @return
     * @throws Exception
     */
    protected static long [] getTargetIds(String inputFile) throws Exception
    {
        TLongArrayList tmp = new TLongArrayList();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(inputFile));
            String line = "";
            while((line = reader.readLine()) != null)
            {
                tmp.add(Long.parseLong(line.trim()));
            }
        }
        finally
        {
            if(reader != null) reader.close();
        }
        return tmp.toArray();
    }
    
    /**
     * Batch mode nearest neighbor search for a list of ids contained in a text file from an index directory
     * @param args
     * <br>-i input text file containing ids to search for, one per line
     * <br>-idx directory containing search index
     * <br>-o output file
     * <br>-b beamwidth to search for in sorted permutation lists
     * <br>-p number of permutations to use 
     * <br>-m minimum cosine similarity to take (-1 will take anything)
     * @throws Exception
     */
    public static void main(String [] args) throws Exception
    {
        CommandLine cmd = new SimpleCli()
        .addOption(new RequiredOption("i", true, "text file list of target ids, one per line"))
        .addOption(new RequiredOption("idx", true, "directory containing index"))
        .addOption(new RequiredOption("o", true, "output file"))
        .addOption(new RequiredOption("b", true, "beamwidth to search for within sorted bitset arrays"))
        .addOption(new RequiredOption("p", true, "number of permutations to use in getting similars"))        
        .addOption(new RequiredOption("m", true, "minimum cosine similarity to take (-1 will take anything)")).parse(args);
        
        NearestNeighborSearcher searcher = new NearestNeighborSearcher(cmd.getOptionValue("idx"));
        int beamWidth = Integer.parseInt(cmd.getOptionValue("b"));
        double minScore = Double.parseDouble(cmd.getOptionValue("m"));
        int numPermutations = Integer.parseInt(cmd.getOptionValue("p"));
        
        long [] targetIds = getTargetIds(cmd.getOptionValue("i"));
        TLongObjectHashMap<Neighbor []> allSims = new TLongObjectHashMap<Neighbor []>();

        Comparator<Neighbor> comparator = new Neighbor.DissimilarityComparator();
        ScoreThreshold thresh = new L2Threshold(minScore);
        for(long id : targetIds)
        {
            Neighbor [] sims = searcher.getScoredSimilars(id, beamWidth, numPermutations, comparator, thresh);
            if(sims != null) allSims.put(id, sims);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
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
        
        System.out.println("NearestNeighborSearcher.java finished.");
    }
}
