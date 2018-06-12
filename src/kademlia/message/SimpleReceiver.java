package kademlia.message;

import java.io.IOException;


public class SimpleReceiver implements Receiver
{

    @Override
    public void receive(Message incoming, int conversationId)
    {
        
    }

    @Override
    public void timeout(int conversationId) throws IOException
    {
        
    }
}
