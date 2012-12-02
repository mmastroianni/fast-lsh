package org.fastlsh.util;

import java.io.File;

public class FileUtils
{
    public static boolean mkdirs(File  dir) throws OutputAlreadyExistsException
    {
        if(dir.exists()) throw new OutputAlreadyExistsException(dir.getAbsolutePath());
        return dir.mkdirs();
    }
}
