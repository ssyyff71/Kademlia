package kademlia.message;

import java.io.IOException;
import kademlia.KadServer;
import kademlia.KademliaNode;


public class ConnectReceiver implements Receiver
{

    private final KadServer server;
    private final KademliaNode localNode;

    public ConnectReceiver(KadServer server, KademliaNode local)
    {
        this.server = server;
        this.localNode = local;
    }

    
    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        ConnectMessage mess = (ConnectMessage) incoming;

        
        this.localNode.getRoutingTable().insert(mess.getOrigin());

        
        AcknowledgeMessage msg = new AcknowledgeMessage(this.localNode.getNode());

        
        this.server.reply(mess.getOrigin(), msg, comm);
    }

    
    @Override
    public void timeout(int comm) throws IOException
    {
    }
}
