/*
   Copyright 2012 Michael Mastroianni, Amol Kapile (fastlsh.org)
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fastlsh.index;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.fastlsh.hash.HashFactory;
import org.fastlsh.hash.HashFamily;
import org.fastlsh.util.BitSetWithId;
import org.fastlsh.util.RequiredOption;
import org.fastlsh.util.SimpleCli;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;

public class RandomProjectionIndexer
{
    public static void main(String [] args) throws ParseException, IOException
    {
        CommandLine cmd = new SimpleCli()
        .addOption(new RequiredOption("i", true, "text file containing .csv of input data"))
        .addOption(new RequiredOption("or", true, "output file containing serialized colt vectors for raw parsed data"))
        .addOption(new RequiredOption("os", true, "output file containing serialized bitset signatures for raw parsed data"))
        .addOption(new RequiredOption("d", true, "dimension of vectors"))
        .addOption(new RequiredOption("sep", true, "separator character delimiting fields in input"))        
        .addOption(new RequiredOption("n", true, "number of hashes in hash family")).parse(args);
        
        int numHashes = Integer.parseInt(cmd.getOptionValue("n"));
        int vecLen = Integer.parseInt(cmd.getOptionValue("d"));
        HashFamily family = new HashFamily(HashFactory.makeProjectionHashFamily(vecLen, numHashes));
        VectorParser parser = new CSVParser(cmd.getOptionValue("sep"));
        BufferedReader reader = null;
        ObjectOutputStream rawStream = null;
        ObjectOutputStream sigStream = null;
        int numLines = 0;
        long start = System.currentTimeMillis();
        try
        {            
            reader = new BufferedReader(new FileReader(cmd.getOptionValue("i")));
            rawStream = new ObjectOutputStream(new FileOutputStream(cmd.getOptionValue("or")));
            sigStream = new ObjectOutputStream(new FileOutputStream(cmd.getOptionValue("os")));
            Algebra alg = new Algebra();
            String line = "";
            while((line = reader.readLine()) != null)
            {
                VectorWithId vec = parser.parse(line);
                double norm = alg.norm2(vec.vector);
                if(norm == 0.0) continue;
                //Compute the signatures non-normalized, but normalize the raw vectors before serialization so that when we check
                // cosine distances, we only have to do dot products
                sigStream.writeObject(new BitSetWithId(vec.id, family.makeSignature(vec.vector)));
                vec.vector.assign(Functions.div(norm));
                rawStream.writeObject(vec);
                numLines++;
                if(numLines%10000 == 0)
                {
                    rawStream.flush();
                    sigStream.flush();
                }
            }
            
        }
        finally
        {
            if(reader != null) reader.close();
            if(rawStream != null) rawStream.close();
            if(sigStream != null) sigStream.close();
        }
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time in seconds: " + ((end -start)/1000));
        System.out.println("Total items: " + numLines);
        System.out.println("input dimensions: " + vecLen);
        System.out.println("number of hashes: " + numHashes);
    }
}
