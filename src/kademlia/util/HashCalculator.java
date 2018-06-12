package kademlia.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class HashCalculator
{

    
    public static byte[] sha1Hash(String toHash) throws NoSuchAlgorithmException
    {
        
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        
        md.update(toHash.getBytes());

        
        return md.digest();
    }

    
    public static byte[] sha1Hash(String toHash, String salt) throws NoSuchAlgorithmException
    {
        
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        
        md.update(toHash.getBytes());

        
        return md.digest(salt.getBytes());
    }

    
    public static byte[] md5Hash(String toHash) throws NoSuchAlgorithmException
    {
        
        MessageDigest md = MessageDigest.getInstance("MD5");

        
        md.update(toHash.getBytes());

        
        return md.digest();
    }

    
    public static byte[] md5Hash(String toHash, String salt) throws NoSuchAlgorithmException
    {
        
        MessageDigest md = MessageDigest.getInstance("MD5");

        
        md.update(toHash.getBytes());

        
        return md.digest(salt.getBytes());
    }
}
