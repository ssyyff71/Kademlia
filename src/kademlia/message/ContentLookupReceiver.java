package kademlia.message;

import java.io.IOException;
import java.util.NoSuchElementException;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.dht.KademliaDHT;


public class ContentLookupReceiver implements Receiver
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KademliaDHT dht;
    private final KadConfiguration config;

    public ContentLookupReceiver(KadServer server, KademliaNode localNode, KademliaDHT dht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
    }

    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        ContentLookupMessage msg = (ContentLookupMessage) incoming;
        this.localNode.getRoutingTable().insert(msg.getOrigin());

        
        if (this.dht.contains(msg.getParameters()))
        {
            try
            {
                
                ContentMessage cMsg = new ContentMessage(localNode.getNode(), this.dht.get(msg.getParameters()));
                server.reply(msg.getOrigin(), cMsg, comm);
            }
            catch (NoSuchElementException ex)
            {
                
            }
        }
        else
        {
            
            NodeLookupMessage lkpMsg = new NodeLookupMessage(msg.getOrigin(), msg.getParameters().getKey());
            new NodeLookupReceiver(server, localNode, this.config).receive(lkpMsg, comm);
        }
    }

    @Override
    public void timeout(int comm)
    {

    }
}
