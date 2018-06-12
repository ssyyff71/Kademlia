package kademlia.dht;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import kademlia.KadConfiguration;
import kademlia.exceptions.ContentExistException;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.node.KademliaId;
import kademlia.util.serializer.JsonSerializer;
import kademlia.util.serializer.KadSerializer;


public class DHT implements KademliaDHT
{

    private transient StoredContentManager contentManager;
    private transient KadSerializer<JKademliaStorageEntry> serializer = null;
    private transient KadConfiguration config;

    private final String ownerId;

    public DHT(String ownerId, KadConfiguration config)
    {
        this.ownerId = ownerId;
        this.config = config;
        this.initialize();
    }

    @Override
    public final void initialize()
    {
        contentManager = new StoredContentManager();
    }

    @Override
    public void setConfiguration(KadConfiguration con)
    {
        this.config = con;
    }

    @Override
    public KadSerializer<JKademliaStorageEntry> getSerializer()
    {
        if (null == serializer)
        {
            serializer = new JsonSerializer<>();
        }

        return serializer;
    }

    @Override
    public boolean store(JKademliaStorageEntry content) throws IOException
    {
        
        if (this.contentManager.contains(content.getContentMetadata()))
        {
            KademliaStorageEntryMetadata current = this.contentManager.get(content.getContentMetadata());

            
            current.updateLastRepublished();

            if (current.getLastUpdatedTimestamp() >= content.getContentMetadata().getLastUpdatedTimestamp())
            {
                
                return false;
            }
            else
            {
                
                try
                {
                    
                    this.remove(content.getContentMetadata());
                }
                catch (ContentNotFoundException ex)
                {
                    
                }
            }
        }

        
        try
        {
            
            
            KademliaStorageEntryMetadata sEntry = this.contentManager.put(content.getContentMetadata());

            
            String contentStorageFolder = this.getContentStorageFolderName(content.getContentMetadata().getKey());

            try (FileOutputStream fout = new FileOutputStream(contentStorageFolder + File.separator + sEntry.hashCode() + ".kct");
                    DataOutputStream dout = new DataOutputStream(fout))
            {
                this.getSerializer().write(content, dout);
            }
            return true;
        }
        catch (ContentExistException e)
        {
            
            return false;
        }
    }

    @Override
    public boolean store(KadContent content) throws IOException
    {
        return this.store(new JKademliaStorageEntry(content));
    }

    @Override
    public JKademliaStorageEntry retrieve(KademliaId key, int hashCode) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        String folder = this.getContentStorageFolderName(key);
        DataInputStream din = new DataInputStream(new FileInputStream(folder + File.separator + hashCode + ".kct"));
        return this.getSerializer().read(din);
    }

    @Override
    public boolean contains(GetParameter param)
    {
        return this.contentManager.contains(param);
    }

    @Override
    public JKademliaStorageEntry get(KademliaStorageEntryMetadata entry) throws IOException, NoSuchElementException
    {
        try
        {
            return this.retrieve(entry.getKey(), entry.hashCode());
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while loading file for content. Message: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("The class for some content was not found. Message: " + e.getMessage());
        }

        
        throw new NoSuchElementException();
    }

    @Override
    public JKademliaStorageEntry get(GetParameter param) throws NoSuchElementException, IOException
    {
        
        try
        {
            KademliaStorageEntryMetadata e = this.contentManager.get(param);
            return this.retrieve(e.getKey(), e.hashCode());
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while loading file for content. Message: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("The class for some content was not found. Message: " + e.getMessage());
        }

        
        throw new NoSuchElementException();
    }

    @Override
    public void remove(KadContent content) throws ContentNotFoundException
    {
        this.remove(new StorageEntryMetadata(content));
    }

    @Override
    public void remove(KademliaStorageEntryMetadata entry) throws ContentNotFoundException
    {
        String folder = this.getContentStorageFolderName(entry.getKey());
        File file = new File(folder + File.separator + entry.hashCode() + ".kct");

        contentManager.remove(entry);

        if (file.exists())
        {
            file.delete();
        }
        else
        {
            throw new ContentNotFoundException();
        }
    }

    
    private String getContentStorageFolderName(KademliaId key)
    {
        
        String folderName = key.hexRepresentation().substring(0, 2);
        File contentStorageFolder = new File(this.config.getNodeDataFolder(ownerId) + File.separator + folderName);

        
        if (!contentStorageFolder.isDirectory())
        {
            contentStorageFolder.mkdir();
        }

        return contentStorageFolder.toString();
    }

    @Override
    public List<KademliaStorageEntryMetadata> getStorageEntries()
    {
        return contentManager.getAllEntries();
    }

    @Override
    public void putStorageEntries(List<KademliaStorageEntryMetadata> ientries)
    {
        for (KademliaStorageEntryMetadata e : ientries)
        {
            try
            {
                this.contentManager.put(e);
            }
            catch (ContentExistException ex)
            {
                
            }
        }
    }

    @Override
    public synchronized String toString()
    {
        return this.contentManager.toString();
    }
}
