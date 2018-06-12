package kademlia.operation;

import kademlia.message.Receiver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import kademlia.JKademliaNode;
import kademlia.dht.GetParameter;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.dht.JKademliaStorageEntry;
import kademlia.dht.KademliaStorageEntry;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.exceptions.RoutingException;
import kademlia.exceptions.UnknownMessageException;
import kademlia.message.ContentLookupMessage;
import kademlia.message.ContentMessage;
import kademlia.message.Message;
import kademlia.message.NodeReplyMessage;
import kademlia.node.KeyComparator;
import kademlia.node.Node;
import kademlia.util.RouteLengthChecker;


public class ContentLookupOperation implements Operation, Receiver
{

    
    private static final Byte UNASKED = (byte) 0x00;
    private static final Byte AWAITING = (byte) 0x01;
    private static final Byte ASKED = (byte) 0x02;
    private static final Byte FAILED = (byte) 0x03;

    private final KadServer server;
    private final JKademliaNode localNode;
    private JKademliaStorageEntry contentFound = null;
    private final KadConfiguration config;

    private final ContentLookupMessage lookupMessage;

    private boolean isContentFound;
    private final SortedMap<Node, Byte> nodes;

    
    private final Map<Integer, Node> messagesTransiting;

    
    private final Comparator comparator;

    
    private final RouteLengthChecker routeLengthChecker;

    
    {
        messagesTransiting = new HashMap<>();
        isContentFound = false;
        routeLengthChecker = new RouteLengthChecker();
    }

    
    public ContentLookupOperation(KadServer server, JKademliaNode localNode, GetParameter params, KadConfiguration config)
    {
        
        this.lookupMessage = new ContentLookupMessage(localNode.getNode(), params);

        this.server = server;
        this.localNode = localNode;
        this.config = config;

        
        this.comparator = new KeyComparator(params.getKey());
        this.nodes = new TreeMap(this.comparator);
    }

    
    @Override
    public synchronized void execute() throws IOException, RoutingException
    {
        try
        {
            
            nodes.put(this.localNode.getNode(), ASKED);

            
            List<Node> allNodes = this.localNode.getRoutingTable().getAllNodes();
            this.addNodes(allNodes);
            
            
            this.routeLengthChecker.addInitialNodes(allNodes);

            
            int totalTimeWaited = 0;
            int timeInterval = 10;     
            while (totalTimeWaited < this.config.operationTimeout())
            {
                if (!this.askNodesorFinish() && !isContentFound)
                {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                }
                else
                {
                    break;
                }
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    
    public void addNodes(List<Node> list)
    {
        for (Node o : list)
        {
            
            if (!nodes.containsKey(o))
            {
                nodes.put(o, UNASKED);
            }
        }
    }

    
    private boolean askNodesorFinish() throws IOException
    {
        
        if (this.config.maxConcurrentMessagesTransiting() <= this.messagesTransiting.size())
        {
            return false;
        }

        
        List<Node> unasked = this.closestNodesNotFailed(UNASKED);

        if (unasked.isEmpty() && this.messagesTransiting.isEmpty())
        {
            
            return true;
        }

        
        Collections.sort(unasked, this.comparator);

        
        for (int i = 0; (this.messagesTransiting.size() < this.config.maxConcurrentMessagesTransiting()) && (i < unasked.size()); i++)
        {
            Node n = (Node) unasked.get(i);

            int comm = server.sendMessage(n, lookupMessage, this);

            this.nodes.put(n, AWAITING);
            this.messagesTransiting.put(comm, n);
        }

        
        return false;
    }

    
    private List<Node> closestNodesNotFailed(Byte status)
    {
        List<Node> closestNodes = new ArrayList<>(this.config.k());
        int remainingSpaces = this.config.k();

        for (Map.Entry e : this.nodes.entrySet())
        {
            if (!FAILED.equals(e.getValue()))
            {
                if (status.equals(e.getValue()))
                {
                    
                    closestNodes.add((Node) e.getKey());
                }

                if (--remainingSpaces == 0)
                {
                    break;
                }
            }
        }

        return closestNodes;
    }

    @Override
    public synchronized void receive(Message incoming, int comm) throws IOException, RoutingException
    {
        if (this.isContentFound)
        {
            return;
        }

        if (incoming instanceof ContentMessage)
        {
            
            ContentMessage msg = (ContentMessage) incoming;

            
            this.localNode.getRoutingTable().insert(msg.getOrigin());

            
            JKademliaStorageEntry content = msg.getContent();
            this.contentFound = content;
            this.isContentFound = true;
        }
        else
        {
            
            NodeReplyMessage msg = (NodeReplyMessage) incoming;

            
            Node origin = msg.getOrigin();
            this.localNode.getRoutingTable().insert(origin);

            
            this.nodes.put(origin, ASKED);

            
            this.messagesTransiting.remove(comm);
            
            
            this.routeLengthChecker.addNodes(msg.getNodes(), origin);

            
            this.addNodes(msg.getNodes());
            this.askNodesorFinish();
        }
    }

    
    @Override
    public synchronized void timeout(int comm) throws IOException
    {
        
        Node n = this.messagesTransiting.get(new Integer(comm));

        if (n == null)
        {
            throw new UnknownMessageException("Unknown comm: " + comm);
        }

        
        this.nodes.put(n, FAILED);
        this.localNode.getRoutingTable().setUnresponsiveContact(n);
        this.messagesTransiting.remove(comm);

        this.askNodesorFinish();
    }
    
    
    public boolean isContentFound()
    {
        return this.isContentFound;
    }

    
    public JKademliaStorageEntry getContentFound() throws ContentNotFoundException
    {
        if (this.isContentFound)
        {
            return this.contentFound;
        }
        else
        {
            throw new ContentNotFoundException("No Value was found for the given key.");
        }
    }

    
    public int routeLength()
    {
        return this.routeLengthChecker.getRouteLength();
    }
}
