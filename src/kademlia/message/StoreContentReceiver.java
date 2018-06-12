package kademlia.message;

import java.io.IOException;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.dht.KademliaDHT;


public class StoreContentReceiver implements Receiver
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KademliaDHT dht;

    public StoreContentReceiver(KadServer server, KademliaNode localNode, KademliaDHT dht)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
    }

    @Override
    public void receive(Message incoming, int comm)
    {
        
        StoreContentMessage msg = (StoreContentMessage) incoming;

        
        this.localNode.getRoutingTable().insert(msg.getOrigin());

        try
        {
            
            this.dht.store(msg.getContent());
        }
        catch (IOException e)
        {
            System.err.println("Unable to store received content; Message: " + e.getMessage());
        }

    }

    @Override
    public void timeout(int comm)
    {
        
    }
}
