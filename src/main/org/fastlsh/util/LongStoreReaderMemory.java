package org.fastlsh.util;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class LongStoreReaderMemory extends LongStoreReader {
	protected DataInputStream file;
	public long [] values;

    public LongStoreReaderMemory(String filename) throws IOException 
    {
        file = new DataInputStream(new FileInputStream(filename));
        length = file.readLong();
        values = new long[(int) length];
        for (int i = 0; i < length; i++) {
        	values[i] = file.readLong();
        }
    }
    
    public synchronized long get(long pos) throws IOException { return values[(int) pos]; } 
    
    public synchronized long [] get(long start, long end) throws IOException 
    {
        int numLongs = (int) (end-start);
        long [] vals = new long[numLongs];
        int start_int = (int) start;
        for(int i = 0; i < numLongs; i++) vals[i] = values[start_int+i];
        return vals;
    }
}
