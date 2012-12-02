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
package org.fastlsh.index;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.fastlsh.parsers.VectorParser;

public abstract class Indexer<T>
{
    IndexOptions options;
    String rootDirName;
    protected VectorParser<T> parser;

    public Indexer(String rootDirName, IndexOptions options)
    {
        this.options = options;
        this.rootDirName = rootDirName;
    }

    public abstract void indexVector(T vector) throws Exception;

    public void setParser(VectorParser<T> parser) {
        this.parser = parser;
    }

    protected void writeOptions() throws IOException 
    {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(new File(rootDirName, Constants.options)));
            out.writeObject(options);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

}
