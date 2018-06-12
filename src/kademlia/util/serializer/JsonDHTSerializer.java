package kademlia.util.serializer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.List;
import kademlia.dht.DHT;
import kademlia.dht.KademliaDHT;
import kademlia.dht.KademliaStorageEntryMetadata;


public class JsonDHTSerializer implements KadSerializer<KademliaDHT>
{

    private final Gson gson;
    private final Type storageEntriesCollectionType;

    
    {
        gson = new Gson();

        storageEntriesCollectionType = new TypeToken<List<KademliaStorageEntryMetadata>>()
        {
        }.getType();
    }

    @Override
    public void write(KademliaDHT data, DataOutputStream out) throws IOException
    {
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out)))
        {
            writer.beginArray();

            
            gson.toJson(data, DHT.class, writer);

            
            gson.toJson(data.getStorageEntries(), this.storageEntriesCollectionType, writer);

            writer.endArray();
        }

    }

    @Override
    public KademliaDHT read(DataInputStream in) throws IOException, ClassNotFoundException
    {
        try (DataInputStream din = new DataInputStream(in);
                JsonReader reader = new JsonReader(new InputStreamReader(in)))
        {
            reader.beginArray();

            
            DHT dht = gson.fromJson(reader, DHT.class);
            dht.initialize();

            
            List<KademliaStorageEntryMetadata> entries = gson.fromJson(reader, this.storageEntriesCollectionType);
            dht.putStorageEntries(entries);

            reader.endArray();
            return dht;
        }
    }
}
