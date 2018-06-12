package kademlia.message;

import java.io.DataInputStream;
import java.io.IOException;
import kademlia.KadServer;


public interface KademliaMessageFactory
{

    
    public Message createMessage(byte code, DataInputStream in) throws IOException;

    
    public Receiver createReceiver(byte code, KadServer server);
}
