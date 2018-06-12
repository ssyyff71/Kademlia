package kademlia.message;

import java.io.IOException;
import java.util.List;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.node.Node;


public class NodeLookupReceiver implements Receiver
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KadConfiguration config;

    public NodeLookupReceiver(KadServer server, KademliaNode local, KadConfiguration config)
    {
        this.server = server;
        this.localNode = local;
        this.config = config;
    }

    
    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        NodeLookupMessage msg = (NodeLookupMessage) incoming;

        Node origin = msg.getOrigin();

        
        this.localNode.getRoutingTable().insert(origin);

        
        List<Node> nodes = this.localNode.getRoutingTable().findClosest(msg.getLookupId(), this.config.k());

        
        Message reply = new NodeReplyMessage(this.localNode.getNode(), nodes);

        if (this.server.isRunning())
        {
            
            this.server.reply(origin, reply, comm);
        }
    }

    
    @Override
    public void timeout(int comm) throws IOException
    {
    }
}
