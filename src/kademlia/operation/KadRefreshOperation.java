package kademlia.operation;

import java.io.IOException;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.dht.KademliaDHT;


public class KadRefreshOperation implements Operation
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KademliaDHT dht;
    private final KadConfiguration config;

    public KadRefreshOperation(KadServer server, KademliaNode localNode, KademliaDHT dht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
    }

    @Override
    public void execute() throws IOException
    {
        
        new BucketRefreshOperation(this.server, this.localNode, this.config).execute();

        
        new ContentRefreshOperation(this.server, this.localNode, this.dht, this.config).execute();
    }
}
