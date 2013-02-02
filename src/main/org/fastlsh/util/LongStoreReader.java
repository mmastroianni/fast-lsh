package org.fastlsh.util;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class LongStoreReader {
    protected static final int sizeOfLong = 8;
    protected static final String mode = "r";

    protected String fileName;
    protected long length;
    
    public static void createLongStore(long [] vals, String fileName) throws IOException
    {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName));
        dos.writeLong((long) vals.length);
        for(long l : vals) dos.writeLong(l);
        dos.close();
    }
    
    public abstract long get(long pos) throws IOException; 
    
    public abstract long [] get(long start, long end) throws IOException;
    
    public long length() {return length;}
}
