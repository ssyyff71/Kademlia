
package kademlia.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import kademlia.message.Streamable;

public class KademliaId implements Streamable, Serializable
{

    public final transient static int ID_LENGTH = 160;
    private byte[] keyBytes;

    
    public KademliaId(String data)
    {
        keyBytes = data.getBytes();
        if (keyBytes.length != ID_LENGTH / 8)
        {
            throw new IllegalArgumentException("Specified Data need to be " + (ID_LENGTH / 8) + " characters long.");
        }
    }

    
    public KademliaId()
    {
        keyBytes = new byte[ID_LENGTH / 8];
        new Random().nextBytes(keyBytes);
    }

    
    public KademliaId(byte[] bytes)
    {
        if (bytes.length != ID_LENGTH / 8)
        {
            throw new IllegalArgumentException("Specified Data need to be " + (ID_LENGTH / 8) + " characters long. Data Given: '" + new String(bytes) + "'");
        }
        this.keyBytes = bytes;
    }

    
    public KademliaId(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    public byte[] getBytes()
    {
        return this.keyBytes;
    }

    
    public BigInteger getInt()
    {
        return new BigInteger(1, this.getBytes());
    }

    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof KademliaId)
        {
            KademliaId nid = (KademliaId) o;
            return this.hashCode() == nid.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + Arrays.hashCode(this.keyBytes);
        return hash;
    }

    
    public KademliaId xor(KademliaId nid)
    {
        byte[] result = new byte[ID_LENGTH / 8];
        byte[] nidBytes = nid.getBytes();

        for (int i = 0; i < ID_LENGTH / 8; i++)
        {
            result[i] = (byte) (this.keyBytes[i] ^ nidBytes[i]);
        }

        KademliaId resNid = new KademliaId(result);

        return resNid;
    }

    
    public KademliaId generateNodeIdByDistance(int distance)
    {
        byte[] result = new byte[ID_LENGTH / 8];

        
        int numByteZeroes = (ID_LENGTH - distance) / 8;
        int numBitZeroes = 8 - (distance % 8);

        
        for (int i = 0; i < numByteZeroes; i++)
        {
            result[i] = 0;
        }

        
        BitSet bits = new BitSet(8);
        bits.set(0, 8);

        for (int i = 0; i < numBitZeroes; i++)
        {
            
            bits.clear(i);
        }
        bits.flip(0, 8);        
        result[numByteZeroes] = (byte) bits.toByteArray()[0];

        
        for (int i = numByteZeroes + 1; i < result.length; i++)
        {
            result[i] = Byte.MAX_VALUE;
        }

        return this.xor(new KademliaId(result));
    }

    
    public int getFirstSetBitIndex()
    {
        int prefixLength = 0;

        for (byte b : this.keyBytes)
        {
            if (b == 0)
            {
                prefixLength += 8;
            }
            else
            {
                
                int count = 0;
                for (int i = 7; i >= 0; i--)
                {
                    boolean a = (b & (1 << i)) == 0;
                    if (a)
                    {
                        count++;
                    }
                    else
                    {
                        break;   
                    }
                }

                
                prefixLength += count;

                
                break;
            }
        }
        return prefixLength;
    }

    
    public int getDistance(KademliaId to)
    {
        
        return ID_LENGTH - this.xor(to).getFirstSetBitIndex();
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        
        out.write(this.getBytes());
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        byte[] input = new byte[ID_LENGTH / 8];
        in.readFully(input);
        this.keyBytes = input;
    }

    public String hexRepresentation()
    {
        
        BigInteger bi = new BigInteger(1, this.keyBytes);
        return String.format("%0" + (this.keyBytes.length << 1) + "X", bi);
    }

    @Override
    public String toString()
    {
        return this.hexRepresentation();
    }

}
