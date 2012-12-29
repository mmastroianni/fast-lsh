package org.fastlsh.index;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.fastlsh.parsers.VectorParser;
import org.fastlsh.util.Signature;
import org.fastlsh.util.LexicographicBitSetComparator;
import org.junit.Assert;

public class IndexUtils
{
    private static final int TEMP_DIR_ATTEMPTS = 10000;

    // lifted from guava
    public static File createTempDir()
    {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++)
        {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir())
            {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within "
                + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName + "0 to "
                + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }

    protected static int generateSingleThreadedIndex(IndexOptions options,
            VectorParser<String> parser, String input, String output)
            throws IOException
    {
        BufferedReader reader = null;
        RandomProjectionSignatureIndexWriter<String> indexer = null;

        try
        {
            indexer = new RandomProjectionSignatureIndexWriter<String>(output, options);
            indexer.setParser(parser);
            reader = new BufferedReader(new FileReader(input));
            String line = "";
            int numLines = 0;
            while ((line = reader.readLine()) != null)
            {
                indexer.indexVector(line.trim());
                numLines++;
            }
            return numLines;
        }
        finally
        {
            if (reader != null)
                reader.close();
            if (indexer != null)
                indexer.close();
        }
    }

    public static void generateMultiThreadedIndex(IndexOptions options,
            org.fastlsh.parsers.VectorParser<String> parser, String input,
            String output) throws Exception
    {
        BufferedReader reader = null;
        ThreadedRandomProjectionIndexer<String> indexer = null;
        try
        {
            indexer = new ThreadedRandomProjectionIndexer<String>(output,
                    options, 16, 10000);
            indexer.setParser(parser);
            reader = new BufferedReader(new FileReader(input));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                indexer.indexVector(line.trim());
            }
        }
        finally
        {
            if (reader != null)
                reader.close();
            if (indexer != null)
                indexer.close();
        }
    }

    static void delete(File f) throws IOException
    {
        if (f.isDirectory())
        {
            for (File c : f.listFiles())
                delete(c);
        }
        f.delete();
    }

    public static void delete(String f)
    {
        try
        {
            File tmp = new File(f);
            tmp.delete();
        }
        catch (Exception e)
        {
        }
    }

    public static boolean areSame(Signature[] sigs1, Signature[] sigs2)
    {
        Comparator<Signature> comp = new LexicographicBitSetComparator();
        Arrays.sort(sigs1, comp);
        Arrays.sort(sigs2, comp);
        for (int i = 0, m = sigs1.length; i < m; i++)
        {
            if (sigs1[i].id != sigs2[i].id)
                return false;
            Assert.assertArrayEquals(sigs1[i].bits.bits, sigs2[i].bits.bits);
        }
        return true;
    }

    public static boolean areSame(TLongObjectHashMap<double[]> map1,
            TLongObjectHashMap<double[]> map2)
    {
        if (map1.size() != map2.size())
            return false;
        TLongObjectIterator<double[]> iter = map1.iterator();
        while (iter.hasNext())
        {
            iter.advance();
            if (!Arrays.equals(iter.value(), map2.get(iter.key())))
                return false;
        }
        return true;
    }

}
