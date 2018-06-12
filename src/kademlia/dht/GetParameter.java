package kademlia.dht;

import kademlia.node.KademliaId;


public class GetParameter
{

    private KademliaId key;
    private String ownerId = null;
    private String type = null;

    
    public GetParameter(KademliaId key, String type)
    {
        this.key = key;
        this.type = type;
    }

    
    public GetParameter(KademliaId key, String type, String owner)
    {
        this(key, type);
        this.ownerId = owner;
    }

    
    public GetParameter(KadContent c)
    {
        this.key = c.getKey();

        if (c.getType() != null)
        {
            this.type = c.getType();
        }

        if (c.getOwnerId() != null)
        {
            this.ownerId = c.getOwnerId();
        }
    }

    
    public GetParameter(KademliaStorageEntryMetadata md)
    {
        this.key = md.getKey();

        if (md.getType() != null)
        {
            this.type = md.getType();
        }

        if (md.getOwnerId() != null)
        {
            this.ownerId = md.getOwnerId();
        }
    }

    public KademliaId getKey()
    {
        return this.key;
    }

    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    public String getOwnerId()
    {
        return this.ownerId;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    @Override
    public String toString()
    {
        return "GetParameter - [Key: " + key + "][Owner: " + this.ownerId + "][Type: " + this.type + "]";
    }
}
