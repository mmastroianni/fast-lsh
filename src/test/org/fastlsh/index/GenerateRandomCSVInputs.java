package org.fastlsh.index;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

import org.fastlsh.util.MathFns;

public class GenerateRandomCSVInputs
{
    static final long seed = 1353123092317l;
    static Random rand = new Random(seed);

    protected static String makeCsv(int id, double [] vals)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        for(double i : vals) sb.append("," + i);
        return sb.toString();
    }
    
    protected static String makeRow(int numDims, int id)
    {
        double [] tmp = new double[numDims];
        for(int i = 0; i < numDims; i++) tmp[i] = rand.nextGaussian();
        MathFns.scalarDivide(tmp, MathFns.norm2(tmp));
        return makeCsv(id, tmp);
    }
    
    public static void main(String [] args) throws Exception
    {
        generateTestFile(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
    }
    
    public static void generateTestFile(int numFeatures, int numRows, String fileName) throws Exception
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        int numDone = 0;        
        do
        {
            writer.write(makeRow(numFeatures, numDone++));
            writer.newLine();
        }
        while(numDone < numRows);
        writer.flush();
        writer.close();
    }

}
