package org.fastlsh.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

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
        file.seek(++pos * sizeOfLong);
        return file.readLong();
    }
    
    public synchronized long [] get(long start, long end) throws IOException 
    {
        int numLongs = (int) (end-start);
        long [] vals = new long[numLongs];
        file.seek(++start* sizeOfLong);
        byte [] bytes = new byte[numLongs * sizeOfLong];
        file.read(bytes);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        for(int i = 0; i < numLongs; i++ ) vals[i] = buffer.getLong();
        return vals;
    }
    
    
    //TODO: Move this to LongStoreReader?
    public void close() throws IOException 
    {
        file.close();
    }
}
