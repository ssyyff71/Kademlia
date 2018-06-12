package kademlia.operation;

import java.io.IOException;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.node.KademliaId;


public class BucketRefreshOperation implements Operation
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KadConfiguration config;

    public BucketRefreshOperation(KadServer server, KademliaNode localNode, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    
    @Override
    public synchronized void execute() throws IOException
    {
        for (int i = 1; i < KademliaId.ID_LENGTH; i++)
        {
            
            final KademliaId current = this.localNode.getNode().getNodeId().generateNodeIdByDistance(i);

            
            new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        new NodeLookupOperation(server, localNode, current, BucketRefreshOperation.this.config).execute();
                    }
                    catch (IOException e)
                    {
                        
                    }
                }
            }.start();
        }
    }
}
