package org.fastlsh.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import gnu.trove.map.hash.TLongObjectHashMap;

import org.fastlsh.util.Signature;
import org.fastlsh.util.FileUtils;
import org.fastlsh.util.LexicographicBitSetComparator;
import org.fastlsh.util.LongStoreReaderDisk;
import org.fastlsh.util.OutputAlreadyExistsException;
import org.fastlsh.util.Permuter;

public class PermutationIndexWriter
{
    String topLevelIndexDir;
    String rootDir;
    Signature [] signatures;
    int numPermutations;
    Permuter permuter;
    TLongObjectHashMap<int []> permutationIndex = new TLongObjectHashMap<int []> ();

    public PermutationIndexWriter(String indexDir, Signature [] signatures, IndexOptions options) throws OutputAlreadyExistsException
    {
        topLevelIndexDir = indexDir;
        File rootDirHandle = new File(topLevelIndexDir, Constants.permutations);
        FileUtils.mkdirs(rootDirHandle);
        rootDir = rootDirHandle.getAbsolutePath();
        this.signatures = signatures;
        this.numPermutations = options.numPermutations;    
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
        for(Signature sig: signatures) permutationIndex.put(sig.id, new int[numPermutations]);
    }
    
    protected void serializePermutationIndex(int idx, long [] values) throws IOException
    {
        File tmp = new File(rootDir, Constants.permutationHead + idx);
        LongStoreReaderDisk.createLongStore(values, tmp.getAbsolutePath());
        
        tmp = new File(rootDir, "textPerms" + idx + ".txt");
        BufferedWriter textWriter = new BufferedWriter(new FileWriter(tmp));
        for (int i = 0, max = values.length; i < max; i++) {
        	textWriter.write(values[i] + "," + signatures[i] + "\n");
        }
        textWriter.flush();
        textWriter.close();
    }
    
    protected void serializeIdMap() throws OutputAlreadyExistsException, FileNotFoundException, IOException
    {
       File tmp = new File(rootDir, Constants.idMap);
       if(tmp.exists()) throw(new OutputAlreadyExistsException(tmp.getAbsolutePath()));
       ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmp));
       oos.writeObject(permutationIndex);
       oos.close();
    }
    
    public static void permuteAndSort(Permuter p, Signature [] sigs)
    {
        p.reset();
        for(int i = 0, max = sigs.length; i < max; i++) sigs[i] = new Signature(sigs[i].id, p.permute(sigs[i].bits));
        Arrays.sort(sigs, new LexicographicBitSetComparator());
    }

    protected void createPermutationIndex(int permId) throws IOException
    {
        permuteAndSort(permuter, signatures);
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
