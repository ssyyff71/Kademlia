package kademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public interface Streamable
{

    
    public void toStream(DataOutputStream out) throws IOException;

    
    public void fromStream(DataInputStream out) throws IOException;
}
