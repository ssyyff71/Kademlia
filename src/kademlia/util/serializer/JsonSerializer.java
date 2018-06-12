package kademlia.util.serializer;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class JsonSerializer<T> implements KadSerializer<T>
{

    private final Gson gson;

    
    {
        gson = new Gson();
    }

    @Override
    public void write(T data, DataOutputStream out) throws IOException
    {
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out)))
        {
            writer.beginArray();

            
            gson.toJson(data.getClass().getName(), String.class, writer);

            
            gson.toJson(data, data.getClass(), writer);

            writer.endArray();
        }
    }

    @Override
    public T read(DataInputStream in) throws IOException, ClassNotFoundException
    {
        try (DataInputStream din = new DataInputStream(in);
                JsonReader reader = new JsonReader(new InputStreamReader(in)))
        {
            reader.beginArray();

            
            String className = gson.fromJson(reader, String.class);

            
            T ret = gson.fromJson(reader, Class.forName(className));
            
            reader.endArray();
            
            return ret;
        }
    }
}
