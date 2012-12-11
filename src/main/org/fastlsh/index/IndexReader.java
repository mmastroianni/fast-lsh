package org.fastlsh.index;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.fastlsh.util.BitSet;
import org.fastlsh.util.BitSetWithId;
import org.fastlsh.util.LongStoreReader;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Class for reading nearest neighbor search indices. An index is presumed to have raw vectors (for now, they are normalized by their l2 norms before being written to disk, in order to make 
 * cosine distance calculations more efficient), a set of permutation lists, and a map from ids to arrays of offsets in the permutations (look up an id in the map, and get an array containing its offset
 * in each of the sorted permutation lists
 * 
 */
public class IndexReader
{
    TLongObjectHashMap<BitSet> sigMap;
    BitSetWithId [] signatures;
    TLongObjectHashMap<double []> rawVectorMap;
    TLongObjectHashMap<int []> permutationIndex = new TLongObjectHashMap<int []> ();
    IndexOptions options;
    String rootDir;
    LongStoreReader [] permutationLists;

    public IndexReader(String rootDir)
    {
        this.rootDir = rootDir;
        
    }
    
    /**
     * On creation, the class simply checks that its directory exists, and that it can read in its metadata object (the Options object: perhaps this should be renamed)
     * @throws InvalidIndexException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void initialize() throws InvalidIndexException, IOException, ClassNotFoundException
    {
        File root = new File(rootDir);
        if(!(root.exists() && root.isDirectory())) throw(new InvalidIndexException(rootDir, "Root dir not present or not a directory"));
        initializeOptions();
    }

    /**
     * Read in meta data object written by the indexer
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected void initializeOptions() throws FileNotFoundException, IOException, ClassNotFoundException
    {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(rootDir, Constants.options)));
        options = (IndexOptions) ois.readObject();
        ois.close();
    }
    
    /**
     * Read the raw vectors off of disk and into RAM. Can read in either a file or a directory, depending on how the indexer was configured
     * @throws InvalidIndexException
     * @throws IOException
     */
    public void initializeRawVecs() throws InvalidIndexException, IOException
    {
        File rawDir = new File(rootDir, Constants.normalizedVectors);
        if(!rawDir.exists()) throw(new InvalidIndexException(rootDir, "Normalized Vectors file not present in this index"));
        rawVectorMap = new TLongObjectHashMap<double []>();

        if(rawDir.isDirectory())
        {
            File[] files = rawDir.listFiles();
            for(File f : files) initializeRawVecs(f.getAbsolutePath());            
        }
        else
        {
            initializeRawVecs(rawDir.getAbsolutePath());
        }
    }

    /**
     * Helper method used by initializeRawVecs(). Reads in vectors for a particular vector in the rawvectors directory if it is a directory.
     * @param file
     * @throws IOException
     */
    private void initializeRawVecs(String file) throws IOException
    {
        ObjectInputStream ois = null;
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
        catch(EOFException e) { /* This always happens when you read multiple objects via objectinputstream. Don't know of any good way around it */ }
        catch (ClassNotFoundException e) { throw new RuntimeException(e); }
        finally { ois.close(); }
    }

    
    /**
     * Initialize LongStoreReader objects for each of the permutation lists
     * @throws InvalidIndexException
     * @throws IOException
     */
    public void initializePermutationLists() throws InvalidIndexException, IOException
    {
        File rawDir = new File(rootDir, Constants.permutations);
        if(!rawDir.exists() || !rawDir.isDirectory()) throw(new InvalidIndexException(rootDir, "No permutation lists found"));
        
        String [] perms = rawDir.list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.contains("perm")? true: false;
            }
        });
        if(perms.length != options.numPermutations) throw(new InvalidIndexException(rootDir, "Expected " + options.numPermutations + " permutation lists, but found " + perms.length)); 
        Arrays.sort(perms);
        permutationLists = new LongStoreReader [options.numPermutations];
        for(int i = 0; i < options.numPermutations; i++)
        {
            String permName = perms[i];
            permutationLists[i] = new LongStoreReader(new File(rawDir, permName).getAbsolutePath());
        }
    }
    
    /**
     * Initialize map from id => [offsets] where the offsets are into the permutation lists
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public void initializePermutationIndex() throws FileNotFoundException, IOException, ClassNotFoundException
    {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(rootDir, Constants.permutations + "/" + Constants.idMap)));
        permutationIndex = (TLongObjectHashMap<int []>) ois.readObject();
        ois.close();
    }
    
    /**
     * Read bitset signatures from disk. This capability is primarily here for testing purposes.
     * @throws InvalidIndexException
     * @throws IOException
     */
    public void initializeSignatures() throws InvalidIndexException, IOException
    {
        File rawDir = new File(rootDir, Constants.signatures);
        if(!rawDir.exists()) throw(new InvalidIndexException(rootDir, "Signatures file not present in this index"));
        sigMap = new TLongObjectHashMap<BitSet>();
        ArrayList<BitSetWithId> tempSigs = new ArrayList<BitSetWithId>();
        if(rawDir.isDirectory())
        {
            File[] files = rawDir.listFiles();
            for(File f : files) initializeSignatures(f.getAbsolutePath(), tempSigs);            
        }
        else initializeSignatures(rawDir.getAbsolutePath(), tempSigs);
        signatures = tempSigs.toArray(new BitSetWithId[tempSigs.size()]);
    }

    /**
     * Helper method for initializeSignatures().
     * @throws InvalidIndexException
     * @throws IOException
     */

    private void initializeSignatures(String file, ArrayList<BitSetWithId> tempSigs) throws IOException
    {
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream(new FileInputStream(file));
            BitSetWithId sig = null;
            do
            {
                sig = (BitSetWithId) ois.readObject();
                tempSigs.add(sig);
                sigMap.put(sig.id, sig.bits);
            }while(sig != null);
        }
        catch(EOFException e) { /* This always happens when you read multiple objects via objectinputstream. Don't know of any good way around it */}
        catch (ClassNotFoundException e) { throw new RuntimeException(e); }
        finally { ois.close(); }
    }
    
}
