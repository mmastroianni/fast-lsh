package org.fastlsh.index;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.fastlsh.util.BitSet;
import org.fastlsh.util.BitSetWithId;

import gnu.trove.map.hash.TLongObjectHashMap;

public class IndexReader
{
    TLongObjectHashMap<BitSet> sigMap;
    BitSetWithId [] signatures;
    TLongObjectHashMap<double []> rawVectorMap;

    String rootDir;
    public IndexReader(String rootDir)
    {
        this.rootDir = rootDir;
        
    }
    
    public void initialize() throws InvalidIndexException, IOException
    {
        File root = new File(rootDir);
        if(!(root.exists() && root.isDirectory())) throw(new InvalidIndexException(rootDir, "Root dir not present or not a directory"));
        
        initializeRawVecs();
        initializeSignatures();
    }

    private void initializeRawVecs() throws InvalidIndexException, IOException
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

    private void initializeSignatures() throws InvalidIndexException, IOException
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
