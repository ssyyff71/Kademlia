package kademlia.operation;

import kademlia.message.Receiver;
import java.io.IOException;
import java.lang.invoke.MethodHandles.Lookup;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.KademliaNode;
import kademlia.exceptions.RoutingException;
import kademlia.message.Message;
import kademlia.message.NodeLookupMessage;
import kademlia.message.NodeReplyMessage;
import kademlia.node.KeyComparator;
import kademlia.node.Node;
import kademlia.util.RouteLengthChecker;
import kademlia.node.KademliaId;
import java.math.*;


public class NodeLookupOperation implements Operation, Receiver
{

    
    private static final String UNASKED = "UnAsked";
    private static final String AWAITING = "Awaiting";
    private static final String ASKED = "Asked";
    private static final String FAILED = "Failed";

    private final KadServer server;
    private final KademliaNode localNode;
    private final KadConfiguration config;

    private final Message lookupMessage;        
    private final Map<Node, String> nodes;

    
    private final Map<Integer, Node> messagesTransiting;

    
    private final Comparator comparator;
    private final RouteLengthChecker routeLengthChecker;
    private KademliaId lookUp;

    
    {
        messagesTransiting = new HashMap<>();
        routeLengthChecker = new RouteLengthChecker();
        
    }

    
    public NodeLookupOperation(KadServer server, KademliaNode localNode, KademliaId lookupId, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
        lookUp = lookupId;
        this.lookupMessage = new NodeLookupMessage(localNode.getNode(), lookupId);

        
        this.comparator = new KeyComparator(lookupId);
        this.nodes = new TreeMap(this.comparator);

    }
    
    public NodeLookupOperation(KadServer server, KademliaNode localNode, KademliaId lookupId, KadConfiguration config,Comparator comparator)
    {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
        lookUp=lookupId;
        this.lookupMessage = new NodeLookupMessage(localNode.getNode(), lookupId);

        
        this.comparator = comparator;
        this.nodes = new TreeMap(this.comparator);
       
    }

    
    @Override
    public synchronized void execute() throws IOException, RoutingException
    {
        try
        {
            
            nodes.put(this.localNode.getNode(), ASKED);

            
            BigInteger b1 = lookUp.getInt();
            b1.xor(this.localNode.getNode().getNodeId().getInt());
            int a = (int) Math.log(b1.doubleValue());


            
            this.addNodes(this.localNode.getRoutingTable().getAllNodes());
            this.routeLengthChecker.addInitialNodes(this.localNode.getRoutingTable().getAllNodes());
            
            int totalTimeWaited = 0;
            int timeInterval = 10;     
            while (totalTimeWaited < this.config.operationTimeout())
            {
                if (!this.askNodesorFinish())
                {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                }
                else
                {
                    break;
                }
            }


            this.localNode.getRoutingTable().setUnresponsiveContacts(this.getFailedNodes());

        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<Node> getClosestNodes()
    {
        return this.closestNodes(ASKED);
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

        
        for (int i = 0; (this.messagesTransiting.size() < this.config.maxConcurrentMessagesTransiting()) && (i < unasked.size()); i++)
        {
            Node n = (Node) unasked.get(i);

            int comm = server.sendMessage(n, lookupMessage, this);

            this.nodes.put(n, AWAITING);
            this.messagesTransiting.put(comm, n);
        }

        
        return false;
    }

    
    private List<Node> closestNodes(String status)
    {
        List<Node> closestNodes = new ArrayList<>(this.config.k());
        int remainingSpaces = this.config.k();

        for (Map.Entry e : this.nodes.entrySet())
        {
            if (status.equals(e.getValue()))
            {
                
                closestNodes.add((Node) e.getKey());
                if (--remainingSpaces == 0)
                {
                    break;
                }
            }
        }

        return closestNodes;

    }
    public  List<Node> Nodes(String status)
    {
        List<Node> closestNodes = new ArrayList<>();

        for (Map.Entry e : this.nodes.entrySet())
        {
            
                
                closestNodes.add((Node) e.getKey());
            
        }

        return closestNodes;

    }

    
    public List<Node> closestNodesNotFailed(String status)
    {
        List<Node> closestNodes = new ArrayList<>(this.config.k());
        int remainingSpaces = this.config.k();

        for (Map.Entry<Node, String> e : this.nodes.entrySet())
        {
            if (!FAILED.equals(e.getValue()))
            {
                if (status.equals(e.getValue()))
                {
                    
                    closestNodes.add(e.getKey());
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
    public synchronized void receive(Message incoming, int comm) throws IOException
    {
        if (!(incoming instanceof NodeReplyMessage))
        {
            
            return;
        }
        
        NodeReplyMessage msg = (NodeReplyMessage) incoming;

        
        Node origin = msg.getOrigin();
        this.localNode.getRoutingTable().insert(origin);

        
        this.nodes.put(origin, ASKED);
        this.routeLengthChecker.addNodes(msg.getNodes(), origin);
        
        this.messagesTransiting.remove(comm);

        
        this.addNodes(msg.getNodes());
        this.askNodesorFinish();
    }

    
    @Override
    public synchronized void timeout(int comm) throws IOException
    {
        
        Node n = this.messagesTransiting.get(comm);

        if (n == null)
        {
            return;
        }

        
        this.nodes.put(n, FAILED);
        this.localNode.getRoutingTable().setUnresponsiveContact(n);
        this.messagesTransiting.remove(comm);

        this.askNodesorFinish();
    }

    public List<Node> getFailedNodes()
    {
        List<Node> failedNodes = new ArrayList<>();

        for (Map.Entry<Node, String> e : this.nodes.entrySet())
        {
            if (e.getValue().equals(FAILED))
            {
                failedNodes.add(e.getKey());
            }
        }

        return failedNodes;
    }
    public int routeLength()
    {
        return this.routeLengthChecker.getRouteLength();
    }
}
