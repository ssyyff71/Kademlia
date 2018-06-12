package kademlia.operation;

import java.io.IOException;
import java.util.List;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.dht.KademliaDHT;
import kademlia.dht.KademliaStorageEntryMetadata;
import kademlia.dht.StorageEntryMetadata;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.message.Message;
import kademlia.message.StoreContentMessage;
import kademlia.node.Node;


public class ContentRefreshOperation implements Operation
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KademliaDHT dht;
    private final KadConfiguration config;

    public ContentRefreshOperation(KadServer server, KademliaNode localNode, KademliaDHT dht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
    }

    
    @Override
    public void execute() throws IOException
    {
        
        List<KademliaStorageEntryMetadata> entries = this.dht.getStorageEntries();

        
        final long minRepublishTime = (System.currentTimeMillis() / 1000L) - this.config.restoreInterval();

        
        for (KademliaStorageEntryMetadata e : entries)
        {
            
            if (e.lastRepublished() > minRepublishTime)
            {
                continue;
            }

            
            e.updateLastRepublished();

            
            List<Node> closestNodes = this.localNode.getRoutingTable().findClosest(e.getKey(), this.config.k());

            
            Message msg = new StoreContentMessage(this.localNode.getNode(), dht.get(e));

            
            for (Node n : closestNodes)
            {
                
                if (!n.equals(this.localNode.getNode()))
                {
                    
                    this.server.sendMessage(n, msg, null);
                }
            }

            
            try
            {
                if (!closestNodes.contains(this.localNode.getNode()))
                {
                    this.dht.remove(e);
                }
            }
            catch (ContentNotFoundException cnfe)
            {
                
                System.err.println("ContentRefreshOperation: Removing content from local node, content not found... Message: " + cnfe.getMessage());
            }
        }

    }
}
