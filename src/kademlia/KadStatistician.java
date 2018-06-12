package kademlia;


public interface KadStatistician
{

    
    public void sentData(long size);

    
    public long getTotalDataSent();

    
    public void receivedData(long size);

    
    public long getTotalDataReceived();

    
    public void setBootstrapTime(long time);

    
    public long getBootstrapTime();

    
    public void addContentLookup(long time, int routeLength, boolean isSuccessful);

    
    public int numContentLookups();

    
    public int numFailedContentLookups();

    
    public long totalContentLookupTime();

    
    public double averageContentLookupTime();

    
    public double averageContentLookupRouteLength();
}
