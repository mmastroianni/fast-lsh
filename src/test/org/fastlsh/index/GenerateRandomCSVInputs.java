package org.fastlsh.index;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class GenerateRandomCSVInputs
{
    static final long seed = 1353123092317l;
    static Random rand = new Random(seed);

    protected static String makeCsv(int id, int [] vals)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        for(int i : vals) sb.append("," + i);
        return sb.toString();
    }
    
    protected static String makeRow(int numDims, int id)
    {
        int [] tmp = new int[numDims];
        for(int i = 0; i < numDims; i++) tmp[i] = (int) (rand.nextFloat()*100);
        return makeCsv(id, tmp);
    }
    
    public static void main(String [] args) throws Exception
    {
        int numFeatures = Integer.parseInt(args[0]);
        int numRows = Integer.parseInt(args[1]);
        BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
        int numDone = 0;        
        do
        {
            writer.write(makeRow(numFeatures, numDone++));
            writer.newLine();
        }
        while(numDone < numRows);
    }
}
