package kademlia.routing;

import java.util.List;
import kademlia.KadConfiguration;
import kademlia.node.Node;
import kademlia.node.KademliaId;


public interface KademliaRoutingTable
{

    
    public void initialize();

    
    public void setConfiguration(KadConfiguration config);

    
    public void insert(Contact c);

    
    public void insert(Node n);

    
    public int getBucketId(KademliaId nid);

    
    public List<Node> findClosest(KademliaId target, int numNodesRequired);

    
    public List getAllNodes();

    
    public List getAllContacts();

    
    public KademliaBucket[] getBuckets();

    
    public void setUnresponsiveContacts(List<Node> contacts);

    
    public void setUnresponsiveContact(Node n);

}
