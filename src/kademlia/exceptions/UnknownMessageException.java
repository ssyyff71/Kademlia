package kademlia.exceptions;


public class UnknownMessageException extends RuntimeException
{

    public UnknownMessageException()
    {
        super();
    }

    public UnknownMessageException(String message)
    {
        super(message);
    }
}
