package kademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kademlia.node.Node;
import kademlia.node.KademliaId;


public class NodeLookupMessage implements Message
{

    private Node origin;
    private KademliaId lookupId;

    public static final byte CODE = 0x05;

    
    public NodeLookupMessage(Node origin, KademliaId lookup)
    {
        this.origin = origin;
        this.lookupId = lookup;
    }

    public NodeLookupMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);
        this.lookupId = new KademliaId(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        this.origin.toStream(out);
        this.lookupId.toStream(out);
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    public KademliaId getLookupId()
    {
        return this.lookupId;
    }

    @Override
    public byte code()
    {
        return CODE;
    }

    @Override
    public String toString()
    {
        return "NodeLookupMessage[origin=" + origin + ",lookup=" + lookupId + "]";
    }
}
