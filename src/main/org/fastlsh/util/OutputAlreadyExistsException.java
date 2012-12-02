package org.fastlsh.util;

public class OutputAlreadyExistsException  extends Exception
{
    private static final long serialVersionUID = -5176347142821114364L;
    public OutputAlreadyExistsException(String dirName)  { super("Output directory already exists: " + dirName + ". Delete this directory if you want to rerun"); }
}
