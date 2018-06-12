package kademlia.operation;

import java.io.IOException;
import kademlia.exceptions.RoutingException;


public interface Operation
{

    
    public void execute() throws IOException, RoutingException;
}
