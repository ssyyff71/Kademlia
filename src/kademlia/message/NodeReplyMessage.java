package kademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import kademlia.node.Node;


public class NodeReplyMessage implements Message
{

    private Node origin;
    public static final byte CODE = 0x06;
    private List<Node> nodes;

    public NodeReplyMessage(Node origin, List<Node> nodes)
    {
        this.origin = origin;
        this.nodes = nodes;
    }

    public NodeReplyMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        
        this.origin = new Node(in);

        
        int len = in.readInt();
        this.nodes = new ArrayList<>(len);

        
        for (int i = 0; i < len; i++)
        {
            this.nodes.add(new Node(in));
        }
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        
        origin.toStream(out);

        
        int len = this.nodes.size();
        if (len > 255)
        {
            throw new IndexOutOfBoundsException("Too many nodes in list to send in NodeReplyMessage. Size: " + len);
        }

        
        out.writeInt(len);
        for (Node n : this.nodes)
        {
            n.toStream(out);
        }
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    @Override
    public byte code()
    {
        return CODE;
    }

    public List<Node> getNodes()
    {
        return this.nodes;
    }

    @Override
    public String toString()
    {
        return "NodeReplyMessage[origin NodeId=" + origin.getNodeId() + "]";
    }
}
