package kademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kademlia.dht.JKademliaStorageEntry;
import kademlia.dht.KademliaStorageEntry;
import kademlia.node.Node;
import kademlia.util.serializer.JsonSerializer;


public class StoreContentMessage implements Message
{

    public static final byte CODE = 0x08;

    private JKademliaStorageEntry content;
    private Node origin;

    
    public StoreContentMessage(Node origin, JKademliaStorageEntry content)
    {
        this.content = content;
        this.origin = origin;
    }

    public StoreContentMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        this.origin.toStream(out);

        
        new JsonSerializer<JKademliaStorageEntry>().write(content, out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);
        try
        {
            this.content = new JsonSerializer<JKademliaStorageEntry>().read(in);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    public JKademliaStorageEntry getContent()
    {
        return this.content;
    }

    @Override
    public byte code()
    {
        return CODE;
    }

    @Override
    public String toString()
    {
        return "StoreContentMessage[origin=" + origin + ",content=" + content + "]";
    }
}
