package kademlia.message;

import java.io.IOException;


public interface Receiver
{

    
    public void receive(Message incoming, int conversationId) throws IOException;

    
    public void timeout(int conversationId) throws IOException;
}
