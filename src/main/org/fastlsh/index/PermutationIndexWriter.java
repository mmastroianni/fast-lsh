package org.fastlsh.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import gnu.trove.map.hash.TLongObjectHashMap;

import org.fastlsh.util.BitSetWithId;
import org.fastlsh.util.FileUtils;
import org.fastlsh.util.LexicographicBitSetComparator;
import org.fastlsh.util.LongStoreReader;
import org.fastlsh.util.OutputAlreadyExistsException;
import org.fastlsh.util.Permuter;

public class PermutationIndexWriter
{
    String topLevelIndexDir;
    String rootDir;
    BitSetWithId [] signatures;
    int numPermutations;
    Permuter permuter;
    TLongObjectHashMap<int []> permutationIndex = new TLongObjectHashMap<int []> ();

    public PermutationIndexWriter(String indexDir, BitSetWithId [] signatures, int numPermutations, IndexOptions options) throws OutputAlreadyExistsException
    {
        this.topLevelIndexDir= rootDir;
        File rootDirHandle = new File(rootDir, Constants.permutations);
        FileUtils.mkdirs(rootDirHandle);
        rootDir = rootDirHandle.getAbsolutePath();
        this.signatures = signatures;
        this.numPermutations = numPermutations;    
        this.permuter = new Permuter(options.numHashes);

        initializeMap();
    }
    
    public void createIndex() throws IOException, OutputAlreadyExistsException
    {
        initializeMap();
        for(int i = 0; i < numPermutations; i++) createPermutationIndex(i);
        serializeIdMap();
    }   
    
    protected void initializeMap()
    {
        for(BitSetWithId sig: signatures) permutationIndex.put(sig.id, new int[numPermutations]);
    }
    
    protected void serializePermutationIndex(int idx, long [] values) throws IOException
    {
        File tmp = new File(rootDir, Constants.permutationHead + 1);
        LongStoreReader.createLongStore(values, tmp.getAbsolutePath());
    }
    
    protected void serializeIdMap() throws OutputAlreadyExistsException, FileNotFoundException, IOException
    {
       File tmp = new File(rootDir, Constants.idMap);
       if(tmp.exists()) throw(new OutputAlreadyExistsException(tmp.getAbsolutePath()));
       ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmp));
       oos.writeObject(permutationIndex);
       oos.close();
    }
    
    public static void permute(Permuter p, BitSetWithId [] sigs)
    {
        p.reset();
        for(int i = 0, max = sigs.length; i < max; i++) sigs[i] = new BitSetWithId(sigs[i].id, p.permute(sigs[i].bits));
        Arrays.sort(sigs, new LexicographicBitSetComparator());
    }

    protected void createPermutationIndex(int permId) throws IOException
    {
        permute(permuter, signatures);
        long [] ids = new long [signatures.length];
        for(int i = 0, m = signatures.length; i < m; i++)
        {
            long sigId = signatures[i].id;
            permutationIndex.get(sigId)[permId] = i;
            ids[i] = sigId;
        }
        serializePermutationIndex(permId, ids);
    }
}
