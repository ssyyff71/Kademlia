package kademlia;


public interface KadConfiguration
{

    
    public long restoreInterval();

    
    public long responseTimeout();

    
    public long operationTimeout();

    
    public int maxConcurrentMessagesTransiting();

    
    public int k();

    
    public int replacementCacheSize();

    
    public int stale();

    
    public String getNodeDataFolder(String ownerId);

    
    public boolean isTesting();
}
