package kademlia.util;

import java.util.Collection;
import java.util.HashMap;
import kademlia.node.Node;


public class RouteLengthChecker
{

    
    private final HashMap<Node, Integer> nodes;

    
    private int maxRouteLength;

    
    {
        this.nodes = new HashMap<>();
        this.maxRouteLength = 1;
    }

    
    public void addInitialNodes(Collection<Node> initialNodes)
    {
        for (Node n : initialNodes)
        {
            this.nodes.put(n, 1);
        }
    }

    
    public void addNodes(Collection<Node> inputSet, Node sender)
    {
        if (!this.nodes.containsKey(sender))
        {
            return;
        }

        
        int inputSetRL = this.nodes.get(sender) + 1;

        if (inputSetRL > this.maxRouteLength)
        {
            this.maxRouteLength = inputSetRL;
        }

        
        for (Node n : inputSet)
        {
            
            if (!this.nodes.containsKey(n))
            {
                this.nodes.put(n, inputSetRL);
            }
        }
    }

    
    public int getRouteLength()
    {
        return this.maxRouteLength;
    }
}
