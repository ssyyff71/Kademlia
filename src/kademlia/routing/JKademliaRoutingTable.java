package kademlia.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import kademlia.KadConfiguration;
import kademlia.node.KeyComparator;
import kademlia.node.Node;
import kademlia.node.KademliaId;


public class JKademliaRoutingTable implements KademliaRoutingTable
{

    private final Node localNode;  
    private transient KademliaBucket[] buckets;

    private transient KadConfiguration config;

    public JKademliaRoutingTable(Node localNode, KadConfiguration config)
    {
        this.localNode = localNode;
        this.config = config;

        
        this.initialize();

        
        this.insert(localNode);
    }

    
    @Override
    public final void initialize()
    {
        this.buckets = new KademliaBucket[KademliaId.ID_LENGTH];
        for (int i = 0; i < KademliaId.ID_LENGTH; i++)
        {
            buckets[i] = new JKademliaBucket(i, this.config);
        }
    }

    @Override
    public void setConfiguration(KadConfiguration config)
    {
        this.config = config;
    }

    
    @Override
    public synchronized final void insert(Contact c)
    {
        this.buckets[this.getBucketId(c.getNode().getNodeId())].insert(c);
    }

    
    @Override
    public synchronized final void insert(Node n)
    {
        this.buckets[this.getBucketId(n.getNodeId())].insert(n);
    }

    
    @Override
    public final int getBucketId(KademliaId nid)
    {
        int bId = this.localNode.getNodeId().getDistance(nid) - 1;

        
        return bId < 0 ? 0 : bId;
    }

    
    @Override
    public synchronized final List<Node> findClosest(KademliaId target, int numNodesRequired)
    {
        TreeSet<Node> sortedSet = new TreeSet<>(new KeyComparator(target));
        sortedSet.addAll(this.getAllNodes());
        
        List<Node> closest = new ArrayList<>(numNodesRequired);

        
        int count = 0;
        for (Node n : sortedSet)
        {
            closest.add(n);
            if (++count == numNodesRequired)
            {
                break;
            }
        }
        return closest;
    }

    
    @Override
    public synchronized final List<Node> getAllNodes()
    {
        List<Node> nodes = new ArrayList<>();

        for (KademliaBucket b : this.buckets)
        {
            for (Contact c : b.getContacts())
            {
                nodes.add(c.getNode());
            }
        }

        return nodes;
    }

    
    @Override
    public final List<Contact> getAllContacts()
    {
        List<Contact> contacts = new ArrayList<>();

        for (KademliaBucket b : this.buckets)
        {
            contacts.addAll(b.getContacts());
        }

        return contacts;
    }

    
    @Override
    public final KademliaBucket[] getBuckets()
    {
        return this.buckets;
    }

    
    public final void setBuckets(KademliaBucket[] buckets)
    {
        this.buckets = buckets;
    }

    
    @Override
    public void setUnresponsiveContacts(List<Node> contacts)
    {
        if (contacts.isEmpty())
        {
            return;
        }
        for (Node n : contacts)
        {
            this.setUnresponsiveContact(n);
        }
    }

    
    @Override
    public synchronized void setUnresponsiveContact(Node n)
    {
        int bucketId = this.getBucketId(n.getNodeId());

        
        this.buckets[bucketId].removeNode(n);
    }

    @Override
    public synchronized final String toString()
    {
        StringBuilder sb = new StringBuilder();
        int totalContacts = 0;
        for (KademliaBucket b : this.buckets)
        {
            if (b.numContacts() > 0)
            {
                totalContacts += b.numContacts();
                sb.append("# nodes in Bucket with depth ");
                sb.append(b.getDepth());
                sb.append(": ");
                sb.append(b.numContacts());
                sb.append("\n");
                sb.append(b.toString());
                sb.append("\n");
            }
        }

        sb.append("\nTotal Contacts: ");
        sb.append(totalContacts);
        sb.append("\n");

     
        return sb.toString();
    }

}
