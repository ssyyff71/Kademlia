package kademlia.dht;

import kademlia.node.KademliaId;


public interface KadContent
{

    
    public KademliaId getKey();

    
    public String getType();

    
    public long getCreatedTimestamp();

    
    public long getLastUpdatedTimestamp();

    
    public String getOwnerId();

    
    public byte[] toSerializedForm();

    
    public KadContent fromSerializedForm(byte[] data);
}
