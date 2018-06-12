package kademlia.dht;


public interface KademliaStorageEntry
{

    
    public void setContent(final byte[] data);

    
    public byte[] getContent();

    
    public KademliaStorageEntryMetadata getContentMetadata();
}
