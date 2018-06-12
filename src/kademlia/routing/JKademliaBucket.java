package kademlia.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import kademlia.KadConfiguration;
import kademlia.node.Node;


public class JKademliaBucket implements KademliaBucket
{

    
    private final int depth;

    
    private final TreeSet<Contact> contacts;

    
    private final TreeSet<Contact> replacementCache;

    private final KadConfiguration config;

    
    {
        contacts = new TreeSet<>();
        replacementCache = new TreeSet<>();
    }

    
    public JKademliaBucket(int depth, KadConfiguration config)
    {
        this.depth = depth;
        this.config = config;
    }

    @Override
    public synchronized void insert(Contact c)
    {
        if (this.contacts.contains(c))
        {
            
            Contact tmp = this.removeFromContacts(c.getNode());
            tmp.setSeenNow();
            tmp.resetStaleCount();
            this.contacts.add(tmp);
        }
        else
        {
            
            if (contacts.size() >= this.config.k())
            {
                
                Contact stalest = null;
                for (Contact tmp : this.contacts)
                {
                    if (tmp.staleCount() >= this.config.stale())
                    {
                        
                        if (stalest == null)
                        {
                            stalest = tmp;
                        }
                        else if (tmp.staleCount() > stalest.staleCount())
                        {
                            stalest = tmp;
                        }
                    }
                }

                
                if (stalest != null)
                {
                    this.contacts.remove(stalest);
                    this.contacts.add(c);
                }
                else
                {
                    
                    this.insertIntoReplacementCache(c);
                }
            }
            else
            {
                this.contacts.add(c);
            }
        }
    }

    @Override
    public synchronized void insert(Node n)
    {
        this.insert(new Contact(n));
    }

    @Override
    public synchronized boolean containsContact(Contact c)
    {
        return this.contacts.contains(c);
    }

    @Override
    public synchronized boolean containsNode(Node n)
    {
        return this.containsContact(new Contact(n));
    }

    @Override
    public synchronized boolean removeContact(Contact c)
    {
        
        if (!this.contacts.contains(c))
        {
            return false;
        }

        
        if (!this.replacementCache.isEmpty())
        {
            
            this.contacts.remove(c);
            Contact replacement = this.replacementCache.first();
            this.contacts.add(replacement);
            this.replacementCache.remove(replacement);
        }
        else
        {
            
            this.getFromContacts(c.getNode()).incrementStaleCount();
        }

        return true;
    }

    private synchronized Contact getFromContacts(Node n)
    {
        for (Contact c : this.contacts)
        {
            if (c.getNode().equals(n))
            {
                return c;
            }
        }

        
        throw new NoSuchElementException("The contact does not exist in the contacts list.");
    }

    private synchronized Contact removeFromContacts(Node n)
    {
        for (Contact c : this.contacts)
        {
            if (c.getNode().equals(n))
            {
                this.contacts.remove(c);
                return c;
            }
        }

        
        throw new NoSuchElementException("Node does not exist in the replacement cache. ");
    }

    @Override
    public synchronized boolean removeNode(Node n)
    {
        return this.removeContact(new Contact(n));
    }

    @Override
    public synchronized int numContacts()
    {
        return this.contacts.size();
    }

    @Override
    public synchronized int getDepth()
    {
        return this.depth;
    }

    @Override
    public synchronized List<Contact> getContacts()
    {
        final ArrayList<Contact> ret = new ArrayList<>();

        
        if (this.contacts.isEmpty())
        {
            return ret;
        }

        
        for (Contact c : this.contacts)
        {
            ret.add(c);
        }

        return ret;
    }
    public synchronized List<Node> getNode()
    {
        final ArrayList<Node> ret = new ArrayList<>();

        
        if (this.contacts.isEmpty())
        {
            return ret;
        }

        
        for (Contact c : this.contacts)
        {
            ret.add(c.getNode());
        }

        return ret;
    }


    
    private synchronized void insertIntoReplacementCache(Contact c)
    {
        
        if (this.replacementCache.contains(c))
        {
            
            Contact tmp = this.removeFromReplacementCache(c.getNode());
            tmp.setSeenNow();
            this.replacementCache.add(tmp);
        }
        else if (this.replacementCache.size() > this.config.k())
        {
            
            this.replacementCache.remove(this.replacementCache.last());
            this.replacementCache.add(c);
        }
        else
        {
            this.replacementCache.add(c);
        }
    }

    private synchronized Contact removeFromReplacementCache(Node n)
    {
        for (Contact c : this.replacementCache)
        {
            if (c.getNode().equals(n))
            {
                this.replacementCache.remove(c);
                return c;
            }
        }

        
        throw new NoSuchElementException("Node does not exist in the replacement cache. ");
    }

    @Override
    public synchronized String toString()
    {
        StringBuilder sb = new StringBuilder("Bucket at depth: ");
        sb.append(this.depth);
        sb.append("\n Nodes: \n");
        for (Contact n : this.contacts)
        {
            sb.append("Node: ");
            sb.append(n.getNode().getNodeId().toString());
            sb.append(" (stale: ");
            sb.append(n.staleCount());
            sb.append(")");
            sb.append("\n");
        }

        return sb.toString();
    }
}
