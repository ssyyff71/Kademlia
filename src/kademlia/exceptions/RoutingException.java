package kademlia.exceptions;

import java.io.IOException;


public class RoutingException extends IOException
{

    public RoutingException()
    {
        super();
    }

    public RoutingException(String message)
    {
        super(message);
    }
}
