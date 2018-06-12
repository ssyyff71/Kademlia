package kademlia.routing;

import java.util.List;
import kademlia.node.Node;


public interface KademliaBucket
{

    
    public void insert(Contact c);

    
    public void insert(Node n);

    
    public boolean containsContact(Contact c);

    
    public boolean containsNode(Node n);

    
    public boolean removeContact(Contact c);

    
    public boolean removeNode(Node n);

    
    public int numContacts();

    
    public int getDepth();

    
    public List<Contact> getContacts();
    public List<Node> getNode();
}
