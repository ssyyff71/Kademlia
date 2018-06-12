package kademlia.routing;

import java.util.Comparator;


public class ContactLastSeenComparator implements Comparator<Contact>
{

    
    @Override
    public int compare(Contact c1, Contact c2)
    {
        if (c1.getNode().equals(c2.getNode()))
        {
            return 0;
        }
        else
        {
            
            return c1.lastSeen() > c2.lastSeen() ? 1 : -1;
        }
    }
}
