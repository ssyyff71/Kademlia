package kademlia.dht;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import kademlia.KadConfiguration;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.node.KademliaId;
import kademlia.util.serializer.KadSerializer;


public interface KademliaDHT
{

    
    public void initialize();

    
    public void setConfiguration(KadConfiguration con);

    
    public KadSerializer<JKademliaStorageEntry> getSerializer();

    
    public boolean store(JKademliaStorageEntry content) throws IOException;

    public boolean store(KadContent content) throws IOException;

    
    public JKademliaStorageEntry retrieve(KademliaId key, int hashCode) throws FileNotFoundException, IOException, ClassNotFoundException;

    
    public boolean contains(GetParameter param);

    
    public JKademliaStorageEntry get(KademliaStorageEntryMetadata entry) throws IOException, NoSuchElementException;

    
    public JKademliaStorageEntry get(GetParameter param) throws NoSuchElementException, IOException;

    
    public void remove(KadContent content) throws ContentNotFoundException;

    public void remove(KademliaStorageEntryMetadata entry) throws ContentNotFoundException;

    
    public List<KademliaStorageEntryMetadata> getStorageEntries();

    
    public void putStorageEntries(List<KademliaStorageEntryMetadata> ientries);

}
