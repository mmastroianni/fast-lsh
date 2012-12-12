/*
   Copyright 2012 Michael Mastroianni, Amol Kapila, Ryan Berdeen (fastlsh.org)
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
package org.fastlsh.parsers;

import org.fastlsh.index.VectorWithId;

/** This class is used to parse a CSV file that encodes dense vectors.
 *  Each line of the file should encode data point.  First should come
 *  the id of the point, then the delimiter, followed by a delimited sequence
 *  of doubles.
 */
public class CSVParser implements VectorParser<String>
{
    private String delim;
    public CSVParser(String d)
    {
        delim = d;
    }
    @Override
    public VectorWithId parse(String line)
    {
        String [] vals = line.trim().split(delim);
        long id = Long.parseLong(vals[0]);
        double [] vec = new double[vals.length-1];
        for(int i = 1, max = vals.length; i < max; i++) vec[i-1] = Double.parseDouble(vals[i]);
        return new VectorWithId(id, vec);
    }
}
