package org.fastlsh.index;

public class InvalidIndexException extends Exception
{
    private static final long serialVersionUID = -3629709668148607186L;

    public InvalidIndexException(String directory, String message)
    {
        super("Invalid index in directory: " + directory + ". Reason: " + message);
    }
}
