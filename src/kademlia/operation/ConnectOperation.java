
package kademlia.operation;

import kademlia.message.Receiver;
import java.io.IOException;
import kademlia.JKademliaNode;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.exceptions.RoutingException;
import kademlia.message.AcknowledgeMessage;
import kademlia.message.ConnectMessage;
import kademlia.message.Message;
import kademlia.node.Node;

public class ConnectOperation implements Operation, Receiver
{

    public static final int MAX_CONNECT_ATTEMPTS = 5;       

    private final KadServer server;
    private final KademliaNode localNode;
    private final Node bootstrapNode;
    private final KadConfiguration config;

    private boolean error;
    private int attempts;

    
    public ConnectOperation(KadServer server, KademliaNode local, Node bootstrap, KadConfiguration config)
    {
        this.server = server;
        this.localNode = local;
        this.bootstrapNode = bootstrap;
        this.config = config;
    }

    @Override
    public synchronized void execute() throws IOException
    {
        try
        {
            
            this.error = true;
            this.attempts = 0;
            Message m = new ConnectMessage(this.localNode.getNode());

            
            server.sendMessage(this.bootstrapNode, m, this);

            
            int totalTimeWaited = 0;
            int timeInterval = 50;     
            while (totalTimeWaited < this.config.operationTimeout())
            {
                if (error)
                {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                }
                else
                {
                    break;
                }
            }
            if (error)
            {
                
                
            }

            
            Operation lookup = new NodeLookupOperation(this.server, this.localNode, this.localNode.getNode().getNodeId(), this.config);
            lookup.execute();

            
            new BucketRefreshOperation(this.server, this.localNode, this.config).execute();
        }
        catch (InterruptedException e)
        {
            System.err.println("Connect operation was interrupted. ");
        }
    }

    
    @Override
    public synchronized void receive(Message incoming, int comm)
    {
        
        AcknowledgeMessage msg = (AcknowledgeMessage) incoming;

        
        this.localNode.getRoutingTable().insert(this.bootstrapNode);

        
        error = false;

        
        notify();
    }

    
    @Override
    public synchronized void timeout(int comm) throws IOException
    {
        if (++this.attempts < MAX_CONNECT_ATTEMPTS)
        {
            this.server.sendMessage(this.bootstrapNode, new ConnectMessage(this.localNode.getNode()), this);
        }
        else
        {
            
            notify();
        }
    }
}
