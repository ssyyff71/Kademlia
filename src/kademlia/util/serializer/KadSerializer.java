package kademlia.util.serializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public interface KadSerializer<T>
{

    
    public void write(T data, DataOutputStream out) throws IOException;

    
    public T read(DataInputStream in) throws IOException, ClassNotFoundException;
}
