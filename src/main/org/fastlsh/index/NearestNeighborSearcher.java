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

import org.apache.commons.cli.CommandLine;
import org.fastlsh.util.LongStoreReader;
import org.fastlsh.util.MathFns;
import org.fastlsh.util.RequiredOption;
import org.fastlsh.util.SimpleCli;

public class NearestNeighborSearcher
{
    IndexReader reader;
    TLongObjectHashMap<double []> rawVectorMap;
    LongStoreReader [] permutationLists;

    int maxPermutations;
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
    
    protected void getSimilars(long pos, int beamRadius, LongStoreReader r, TLongHashSet output) throws IOException
    {
        long max = Math.min(r.length(), (long)pos+beamRadius);
        long min = Math.max(0, (long)pos-beamRadius);
        long [] ids = r.get(min, max);
        output.addAll(ids);
    }
    
    public LongDoublePair [] getDistances(long srcId, long [] inputs)
    {
        return getDistances(srcId, inputs, 0.0);
    }
    
    public LongDoublePair [] getDistances(long srcId, long [] inputs, double minScore)
    {
        double [] src = rawVectorMap.get(srcId);
        if(src == null) return null;
        ArrayList<LongDoublePair> tmp = new ArrayList<LongDoublePair>();
        for(int i = 0, max = inputs.length; i < max; i++)
        {
            long target = inputs[i];
            double [] targetv = rawVectorMap.get(target);
            double score = targetv == null? 0.0: MathFns.dot(src, targetv);
            if(score > minScore) tmp.add(new LongDoublePair(target, score));
        }
        
        return tmp.toArray(new LongDoublePair[tmp.size()]);
    }
    
    public LongDoublePair[] getScoredSimilars(long id, int beamRadius, int numPermutations, double minScore) throws InvalidIndexException, IOException
    {
        
        LongDoublePair[] retval = getDistances(id, getSimilars(id, beamRadius, numPermutations), minScore);
        if (retval != null) Arrays.sort(retval, new LongDoublePair.DescendingDComparator());
        return retval;
    }

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
        TLongObjectHashMap<LongDoublePair []> allSims = new TLongObjectHashMap<LongDoublePair []>();

        for(long id : targetIds)
        {
            LongDoublePair [] sims = searcher.getScoredSimilars(id, beamWidth, numPermutations, minScore);
            if(sims != null) allSims.put(id, sims);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
        TLongObjectIterator<LongDoublePair []> ids = allSims.iterator();
        while(ids.hasNext()) {
        	ids.advance();
        	long id = ids.key();
        	writer.write(id + "\n--------------\n");
        	LongDoublePair [] sims = allSims.get(id);
        	for(LongDoublePair similar : sims) writer.write(similar.l + "," + similar.d + "\n");
        	writer.write("\n\n");
        }
        writer.flush();
        writer.close();
        
        System.out.println("NearestNeighborSearcher.java finished.");
    }
}
