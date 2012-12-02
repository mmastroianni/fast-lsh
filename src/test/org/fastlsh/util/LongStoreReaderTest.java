package org.fastlsh.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class LongStoreReaderTest
{
    @Test
    public void test() throws IOException
    {
        File testFile = File.createTempFile("testLongStoreReader", "dat");
        testFile.deleteOnExit();
        String fileName = testFile.getAbsolutePath();
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(testFile));
        long len = 100l;
        dos.writeLong(len);
        long [] testArr = new long[(int)len];
        for(int i = 0; i < len; i++)
        {
            dos.writeLong((long)i);
            testArr[i] = (long)i;
        }
        dos.close();
        LongStoreReader reader = new LongStoreReader(fileName);
        Assert.assertEquals(100, reader.length());
        for(int i = 0; i < len; i++) Assert.assertEquals(i, reader.get(i));
        long [] foo = reader.get(0, reader.length());
        Assert.assertTrue(Arrays.equals(testArr, foo));

        testArr = new long[10];
        for(int i = 0; i < 10; i++)
        {
            testArr[i] = (long)(i+15);
        }
        foo = reader.get(15, 25);
        Assert.assertTrue(Arrays.equals(testArr, foo));
    }
}
