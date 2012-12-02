package org.fastlsh.util;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LongStoreReader
{
    static final int sizeofLong = 8;
    static final String mode = "r";

    protected String fileName;
    private RandomAccessFile file;
    long length;

    public static void createLongStore(long [] vals, String fileName) throws IOException
    {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName));
        dos.writeLong((long) vals.length);
        for(long l : vals) dos.writeLong(l);
        dos.close();
    }
    
    public LongStoreReader(String filename) throws IOException 
    {
        file = new RandomAccessFile(filename, mode);
        length = file.readLong();
    }
    
    public long length() {return length;}
    
    public synchronized long get(long pos) throws IOException 
    {
        file.seek(++pos * sizeofLong);
        return file.readLong();
    }
    
    public synchronized long [] get(long start, long end) throws IOException 
    {
        int numLongs = (int) (end -start);
        long [] vals = new long[numLongs];
        file.seek(++start* sizeofLong);
        for(int i = 0; i < numLongs; i++ ) vals[i] = file.readLong();
        return vals;
    }
    
    public void close() throws IOException 
    {
        file.close();
    }

}
