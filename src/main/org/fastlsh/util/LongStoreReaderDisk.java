package org.fastlsh.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class LongStoreReaderDisk extends LongStoreReader
{
	protected RandomAccessFile file;
	
    public LongStoreReaderDisk(String filename) throws IOException 
    {
        file = new RandomAccessFile(filename, mode);
        length = file.readLong();
    }
    
    public synchronized long get(long pos) throws IOException 
    {
        file.seek(++pos * sizeofLong);
        return file.readLong();
    }
    
    public synchronized long [] get(long start, long end) throws IOException 
    {
        int numLongs = (int) (end-start);
        long [] vals = new long[numLongs];
        file.seek(++start* sizeofLong);
        for(int i = 0; i < numLongs; i++ ) vals[i] = file.readLong();
        return vals;
    }
    
    
    //TODO: Move this to LongStoreReader?
    public void close() throws IOException 
    {
        file.close();
    }
}
