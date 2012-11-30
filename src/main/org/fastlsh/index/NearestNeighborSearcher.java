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

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.fastlsh.util.BitSetWithId;
import org.fastlsh.util.LexicographicBitSetComparator;
import org.fastlsh.util.Permuter;
import org.fastlsh.util.RequiredOption;
import org.fastlsh.util.SimpleCli;

public class NearestNeighborSearcher
{
    BitSetWithId [] signatures;
    TLongObjectHashMap<BitSetWithId> sigMap;
    TLongObjectHashMap<double []> rawVectorMap;
    Permuter permuter;
    private int numBits;
    
    private void initializeRawVecs(String file) throws IOException
    {
        ObjectInputStream ois = null;
        rawVectorMap = new TLongObjectHashMap<double []>();
        try
        {
            ois = new ObjectInputStream(new FileInputStream(file));
            VectorWithId vec = null;
            do
            {
                vec = (VectorWithId) ois.readObject();
                rawVectorMap.put(vec.id, vec.vals);
            }while(vec != null);
        }
        catch(EOFException e)
        {
            //This always happens when you read multiple objects via objectinputstream. Don't know of any good way around it
        }
        catch (ClassNotFoundException e)
        {
			throw new RuntimeException(e);
		}
        finally
        {
            ois.close();
        }
    }

    public static void permute(Permuter p, BitSetWithId [] sigs)
    {
        p.reset();
        for(int i = 0, max = sigs.length; i < max; i++) sigs[i] = new BitSetWithId(sigs[i].id, p.permute(sigs[i].bits));
        Arrays.sort(sigs, new LexicographicBitSetComparator());
        
    }
    
    public void permute()
    {
        permute(permuter, signatures);
    }
    
    private void initializeSignatures(String file) throws IOException
    {
        ObjectInputStream ois = null;
        ArrayList<BitSetWithId> tempSigs = new ArrayList<BitSetWithId>();
        sigMap = new TLongObjectHashMap<BitSetWithId>();
        try
        {
            ois = new ObjectInputStream(new FileInputStream(file));
            BitSetWithId sig = null;
            do
            {
                sig = (BitSetWithId) ois.readObject();
                tempSigs.add(sig);
                sigMap.put(sig.id, sig);
            }while(sig != null);
        }
        catch(EOFException e)
        {
            //This always happens when you read multiple objects via objectinputstream. Don't know of any good way around it
        }
        catch (ClassNotFoundException e)
        {
			throw new RuntimeException(e);
		}
        finally
        {
            ois.close();
        }
        signatures = tempSigs.toArray(new BitSetWithId[tempSigs.size()]);
    }
    
    public NearestNeighborSearcher(String bitSetFile, String rawVecFile) throws FileNotFoundException, IOException
    {
        initializeRawVecs(rawVecFile);
        initializeSignatures(bitSetFile);
        numBits = signatures[0].bits.numBits;
        permuter = new Permuter(numBits);
    }

    public long [] getSimilars(long id, int beamRadius)
    {
        BitSetWithId search = sigMap.get(id);
        if(search == null) return null;
        TLongArrayList retval = new TLongArrayList();
        
        int idx = Arrays.binarySearch(signatures, search, new LexicographicBitSetComparator());
        int bottom = Math.max(idx-beamRadius, 0);
        int top = Math.min(idx+beamRadius, signatures.length);
        
        for(int i = bottom; i < top; i++)
        {
            retval.add(signatures[i].id);
        }
        return retval.toArray();
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
            if(target == srcId) continue;
            double [] targetv = rawVectorMap.get(srcId);
            double score = targetv == null? 0.0: VectorWithId.dotProduct(src, targetv);
            if(score > minScore) tmp.add(new LongDoublePair(target, score));
        }
        
        return tmp.toArray(new LongDoublePair[tmp.size()]);
    }
    
    public LongDoublePair[] getScoredSimilars(long id, int beamRadius, double minScore)
    {
        LongDoublePair[] retval = getDistances(id, getSimilars(id, beamRadius), minScore);
        Arrays.sort(retval, new LongDoublePair.DescendingDComparator());
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
        .addOption(new RequiredOption("r", true, "file containing serialized raw vectors"))
        .addOption(new RequiredOption("s", true, "file containing serialized bitset signatures"))
        .addOption(new RequiredOption("b", true, "beamwidth to search for within sorted bitset arrays"))
        .addOption(new RequiredOption("p", true, "number of permutations to use in getting similars"))        
        .addOption(new RequiredOption("m", true, "minimum cosine similarity to take (0.0 will take anything)")).parse(args);
        
        NearestNeighborSearcher searcher = new NearestNeighborSearcher(cmd.getOptionValue("s"), cmd.getOptionValue("r"));
        int beamWidth = Integer.parseInt(cmd.getOptionValue("b"));
        double minScore = Double.parseDouble(cmd.getOptionValue("m"));
        int numPermutations = Integer.parseInt(cmd.getOptionValue("p"));
        
        long [] targetIds = getTargetIds(cmd.getOptionValue("i"));
        TLongObjectHashMap<HashSet<LongDoublePair>> allSims = new TLongObjectHashMap<HashSet<LongDoublePair>>();
        for(int i = 0; i < numPermutations; i++)
        {
            searcher.permute();
            for(long tid : targetIds)
            {
                HashSet<LongDoublePair> sims = allSims.get(tid);
                if(sims == null)
                {
                    sims = new HashSet<LongDoublePair>();
                    allSims.put(tid, sims);
                }
                LongDoublePair [] newSims = searcher.getScoredSimilars(tid, beamWidth, minScore);
                sims.addAll(Arrays.asList(newSims));
            }
        }
        
        //TODO: serialize output
    }
    
}
