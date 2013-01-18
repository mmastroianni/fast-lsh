package org.fastlsh.query;

public class QueryNotInIndexException extends Exception
{
    private static final long serialVersionUID = -3629709668123607186L;

    public QueryNotInIndexException(String message)
    {
        super(message);
    }
}
