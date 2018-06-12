
package kademlia.operation;

import java.io.IOException;
import kademlia.KadServer;
import kademlia.exceptions.RoutingException;
import kademlia.node.Node;

public class PingOperation implements Operation
{

    private final KadServer server;
    private final Node localNode;
    private final Node toPing;

    
    public PingOperation(KadServer server, Node local, Node toPing)
    {
        this.server = server;
        this.localNode = local;
        this.toPing = toPing;
    }

    @Override
    public void execute() throws IOException, RoutingException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
