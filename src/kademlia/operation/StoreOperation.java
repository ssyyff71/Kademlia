package kademlia.operation;

import java.io.IOException;
import java.util.List;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.dht.JKademliaStorageEntry;
import kademlia.dht.KademliaDHT;
import kademlia.dht.KademliaStorageEntry;
import kademlia.message.Message;
import kademlia.message.StoreContentMessage;
import kademlia.node.Node;


public class StoreOperation implements Operation
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final JKademliaStorageEntry storageEntry;
    private final KademliaDHT localDht;
    private final KadConfiguration config;

    
    public StoreOperation(KadServer server, KademliaNode localNode, JKademliaStorageEntry storageEntry, KademliaDHT localDht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.storageEntry = storageEntry;
        this.localDht = localDht;
        this.config = config;
    }

    @Override
    public synchronized void execute() throws IOException
    {
        
        NodeLookupOperation ndlo = new NodeLookupOperation(this.server, this.localNode, this.storageEntry.getContentMetadata().getKey(), this.config);
        ndlo.execute();
        List<Node> nodes = ndlo.getClosestNodes();

        
        Message msg = new StoreContentMessage(this.localNode.getNode(), this.storageEntry);

        
        for (Node n : nodes)
        {
            if (n.equals(this.localNode.getNode()))
            {
                
                this.localDht.store(this.storageEntry);
            }
            else
            {
                
                this.server.sendMessage(n, msg, null);
            }
        }
    }

    
    public int numNodesStoredAt()
    {
        return 1;
    }
}
