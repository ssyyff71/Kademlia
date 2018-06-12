package kademlia.dht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import kademlia.exceptions.ContentExistException;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.node.KademliaId;


class StoredContentManager
{

    private final Map<KademliaId, List<KademliaStorageEntryMetadata>> entries;

    
    {
        entries = new HashMap<>();
    }

    
    public KademliaStorageEntryMetadata put(KadContent content) throws ContentExistException
    {
        return this.put(new StorageEntryMetadata(content));
    }

    
    public KademliaStorageEntryMetadata put(KademliaStorageEntryMetadata entry) throws ContentExistException
    {
        if (!this.entries.containsKey(entry.getKey()))
        {
            this.entries.put(entry.getKey(), new ArrayList<>());
        }

        
        if (!this.contains(entry))
        {
            this.entries.get(entry.getKey()).add(entry);

            return entry;
        }
        else
        {
            throw new ContentExistException("Content already exists on this DHT");
        }
    }

    
    public synchronized boolean contains(GetParameter param)
    {
        if (this.entries.containsKey(param.getKey()))
        {
            
            for (KademliaStorageEntryMetadata e : this.entries.get(param.getKey()))
            {
                
                if (e.satisfiesParameters(param))
                {
                    return true;
                }
            }
        }
        else
        {
        }
        return false;
    }

    
    public synchronized boolean contains(KadContent content)
    {
        return this.contains(new GetParameter(content));
    }

    
    public synchronized boolean contains(KademliaStorageEntryMetadata entry)
    {
        return this.contains(new GetParameter(entry));
    }

    
    public KademliaStorageEntryMetadata get(GetParameter param) throws NoSuchElementException
    {
        if (this.entries.containsKey(param.getKey()))
        {
            
            for (KademliaStorageEntryMetadata e : this.entries.get(param.getKey()))
            {
                
                if (e.satisfiesParameters(param))
                {
                    return e;
                }
            }

            
            throw new NoSuchElementException();
        }
        else
        {
            throw new NoSuchElementException("No content exist for the given parameters");
        }
    }

    public KademliaStorageEntryMetadata get(KademliaStorageEntryMetadata md)
    {
        return this.get(new GetParameter(md));
    }

    
    public synchronized List<KademliaStorageEntryMetadata> getAllEntries()
    {
        List<KademliaStorageEntryMetadata> entriesRet = new ArrayList<>();

        for (List<KademliaStorageEntryMetadata> entrySet : this.entries.values())
        {
            if (entrySet.size() > 0)
            {
                entriesRet.addAll(entrySet);
            }
        }

        return entriesRet;
    }

    public void remove(KadContent content) throws ContentNotFoundException
    {
        this.remove(new StorageEntryMetadata(content));
    }

    public void remove(KademliaStorageEntryMetadata entry) throws ContentNotFoundException
    {
        if (contains(entry))
        {
            this.entries.get(entry.getKey()).remove(entry);
        }
        else
        {
            throw new ContentNotFoundException("This content does not exist in the Storage Entries");
        }
    }

    @Override
    public synchronized String toString()
    {
        StringBuilder sb = new StringBuilder("Stored Content: \n");
        int count = 0;
        for (List<KademliaStorageEntryMetadata> es : this.entries.values())
        {
            if (entries.size() < 1)
            {
                continue;
            }

            for (KademliaStorageEntryMetadata e : es)
            {
                sb.append(++count);
                sb.append(". ");
                sb.append(e);
                sb.append("\n");
            }
        }

        sb.append("\n");
        return sb.toString();
    }
}
