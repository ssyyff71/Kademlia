package kademlia.dht;

import kademlia.node.KademliaId;


public interface KademliaStorageEntryMetadata
{

    
    public KademliaId getKey();

    
    public String getOwnerId();

    
    public String getType();

    
    public int getContentHash();

    
    public long getLastUpdatedTimestamp();

    
    public boolean satisfiesParameters(GetParameter params);

    
    public long lastRepublished();

    
    public void updateLastRepublished();
}
