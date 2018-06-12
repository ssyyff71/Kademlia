package kademlia.exceptions;


public class KadServerDownException extends RoutingException
{

    public KadServerDownException()
    {
        super();
    }

    public KadServerDownException(String message)
    {
        super(message);
    }
}
