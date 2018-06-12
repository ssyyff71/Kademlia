package kademlia.node;

import java.math.BigInteger;
import java.util.Comparator;


public class KeyComparator2 implements Comparator<Node>
{

    private final BigInteger key;
    private final String KeyS;
    
    public KeyComparator2(KademliaId key)
    {
    	this.KeyS=key.toString();
        this.key = key.getInt();
    }

    
    @Override
    public int compare(Node n1, Node n2)
    {
        BigInteger b1 = n1.getNodeId().getInt();
        BigInteger b2 = n2.getNodeId().getInt();

        b1 = b1.xor(key);
        b2 = b2.xor(key);
       if(b1.abs().compareTo(b2.abs())==1) {
        System.out.println(n2.getNodeId()+"\'s distance to "+KeyS+"is "+b2);
       }
       else {
    	   System.out.println(n1.getNodeId()+"\'s distance to "+KeyS+"is "+b1);
              
       }
       
        return b1.abs().compareTo(b2.abs());
    }
}
