package kademlia;

import java.io.IOException;
import java.util.NoSuchElementException;
import kademlia.dht.GetParameter;
import kademlia.dht.JKademliaStorageEntry;
import kademlia.dht.KadContent;
import kademlia.dht.KademliaDHT;
import kademlia.dht.KademliaStorageEntry;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.exceptions.RoutingException;
import kademlia.node.Node;
import kademlia.routing.KademliaRoutingTable;


public interface KademliaNode
{

    
    public void startRefreshOperation();

    
    public void stopRefreshOperation();

    
    public Node getNode();

    
    public KadServer getServer();

    
    public KademliaDHT getDHT();

    
    public KadConfiguration getCurrentConfiguration();

    
    public void bootstrap(Node n) throws IOException, RoutingException;

    
    public int put(KadContent content) throws IOException;

    
    public int put(JKademliaStorageEntry entry) throws IOException;

    
    public void putLocally(KadContent content) throws IOException;

    
    public JKademliaStorageEntry get(GetParameter param) throws NoSuchElementException, IOException, ContentNotFoundException;

    
    public void refresh() throws IOException;

    
    public String getOwnerId();

    
    public int getPort();

    
    public void shutdown(final boolean saveState) throws IOException;

    
    public void saveKadState() throws IOException;

    
    public KademliaRoutingTable getRoutingTable();

    
    public KadStatistician getStatistician();
    public void findNode(KademliaNode val);
}
