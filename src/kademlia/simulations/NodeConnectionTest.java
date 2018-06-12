package kademlia.simulations;

import java.io.IOException;

import kademlia.node.KademliaId;
import kademlia.node.KeyComparator2;
import kademlia.node.Node;
import kademlia.operation.NodeLookupOperation;
import kademlia.operation.Operation;

import java.awt.print.Printable;
import java.io.BufferedReader;  
import java.io.FileInputStream;  
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Scanner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.xalan.internal.lib.NodeInfo;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
import kademlia.*;
import java.io.*;
public class NodeConnectionTest
{
	public static JKademliaNode jNode[]=new JKademliaNode[40];
	public static JsonArray  nodeInfoArray;
	public static void main(String [] args) {
		NodeConnectionTest nodeConnectionTest = new NodeConnectionTest();
		String aString = nodeConnectionTest.Find();
		//System.out.println(aString);
	
		}
	public  String Find()
	{
        try
        {   
        	String nodeData[][] =new String[800][2];
        	//JKademliaNode jNode[]=new JKademliaNode[115];
        	FileInputStream inputStream = new FileInputStream("data.txt");  
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));  
            String str = null;  
            for(int i=0;i<jNode.length;i++)
            {
            	str= bufferedReader.readLine();
            	nodeData[i][0]=str.split(" ")[0];
            	nodeData[i][1]=str.split(" ")[1];
//            	System.out.println(i);
            	jNode[i]=new JKademliaNode(nodeData[i][0], new KademliaId(),Integer.parseInt(nodeData[i][1]));
            	 System.out.println("Created Node Kad "+nodeData[i][0]+":"+ jNode[i].getNode().getNodeId());
    
            }
//            System.out.println(jNode[14].getNode().getNodeId());
                  
            //close  
            inputStream.close();  
            bufferedReader.close();  

//            System.out.println("Connecting Kad 1 and Kad 2");
            int i = 1;
           
            for(;i<jNode.length/3;i++) {
//            jNode[0].getRoutingTable().insert(jNode[i].getNode());
            jNode[0].bootstrap(jNode[i].getNode());
            System.out.println(jNode[0].getRoutingTable());
            System.out.println(i);
            }
            for(;i<jNode.length/3*2;i++) {
//            	jNode[1].getRoutingTable().insert(jNode[i].getNode());
            	jNode[1].bootstrap(jNode[i].getNode());
            	System.out.println(jNode[1].getRoutingTable());
           	System.out.println(i);
            }
            for(;i<jNode.length;i++) {
//            	jNode[2].getRoutingTable().insert(jNode[i].getNode());
            	jNode[2].bootstrap(jNode[i].getNode());
            	System.out.println(jNode[2].getRoutingTable());
           	System.out.println(i);
            }
            
            JsonArray  routerTableArray = new JsonArray();
            
            
            
            for(int j = 0;j<jNode.length;j++) {
            	JsonObject routerTable= new JsonObject();
                routerTable.addProperty(String.valueOf(j),jNode[j].getRoutingTable().toString());
            	routerTableArray.add(routerTable);
            	
            System.out.println(jNode[j].getRoutingTable());
            //System.out.println(jNode[1].getRoutingTable());
            }
            
            File f = new java.io.File("./router1.txt");
            FileOutputStream fOutputStream = new FileOutputStream(f);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOutputStream);
            for(int j = 0;j<jNode.length;j++) {
            	JsonObject routerTable= new JsonObject();
                routerTable.addProperty(String.valueOf(j),jNode[j].getRoutingTable().toString());
            	routerTableArray.add(routerTable);
               outputStreamWriter.write(jNode[j].getRoutingTable().toString()+'\n');
               outputStreamWriter.flush();
            }
            outputStreamWriter.close();
            fOutputStream.close();
            
            return routerTableArray.toString() ;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return e.toString();
        }
    }
    public String FindNodeBegin(int nodeId,int val1,int number) {
    	nodeInfoArray = new JsonArray();
    	FindNoderoads(nodeId, val1,nodeInfoArray,number);
    	return nodeInfoArray.toString();
    }
	public  void FindNoderoads(int nodeId,int val1,JsonArray nodeInfoArray,int number) {
    	   BigInteger b1 = jNode[val1].getNode().getNodeId().getInt();
           b1.xor(jNode[nodeId].getNode().getNodeId().getInt());
           JsonObject nodeInfo = new JsonObject();
           nodeInfo.addProperty("1", "本节点id:"+jNode[nodeId].getNode().getNodeId().toString());
           nodeInfo.addProperty("2", "所找节点id:"+jNode[val1].getNode().getNodeId().toString());
           nodeInfo.addProperty("3", "节点距离:"+b1.toString());
//           System.out.println("本节点id为"+jNode[nodeId].getNode().getNodeId()+"\n");
//           System.out.println("所找节点ID为："+jNode[val1].getNode().getNodeId()+"   节点距离为："+b1+"\n");
           int a =jNode[nodeId].getRoutingTable().getBucketId(jNode[val1].getNode().getNodeId());
           //int a = (int) (Math.log(b1.doubleValue())/Math.log(2));
           nodeInfo.addProperty("4","k桶:"+String.valueOf(a));
           nodeInfo.addProperty("5","桶内节点:"+jNode[nodeId].getRoutingTable().getBuckets()[a].getNode().toString());
//           System.out.println("k桶"+a+"内的信息为"+jNode[nodeId].getRoutingTable().getBuckets()[a].getNode().toString());
           
           
           if(jNode[nodeId].getRoutingTable().getBuckets()[a].getNode().contains(jNode[val1].getNode())) {
//           	System.out.println(jNode[val1].getOwnerId()+" "+jNode[val1].getPort());
           	nodeInfo.addProperty("6", "IP:"+jNode[val1].getOwnerId());
           	nodeInfo.addProperty("7", "Port:"+String.valueOf(jNode[val1].getPort()));
           	nodeInfoArray.add(nodeInfo);
           	return;
           }
         
           else {
//           NodeLookupOperation lookup = new NodeLookupOperation(jNode[0].getServer(),jNode[0],jNode[val1].getNode().getNodeId(),new DefaultConfiguration(),new KeyComparator2(jNode[val1].getNode().getNodeId()));
//           lookup.execute();
//        	System.out.println("寻找最近的3个节点：\n");
        	
           	java.util.List<Node> findNodes= jNode[nodeId].getRoutingTable().findClosest(jNode[val1].getNode().getNodeId(),number);
            String as = new String();
           	for (int i=0;i<findNodes.size();i++) {
           		as+=","+findNodes.get(i).getNodeId();
//           		System.out.print(findNodes.get(i).getNodeId()+"\n");	
           }
           
           nodeInfo.addProperty("6", "最近的"+number+"个节点:"+as);
           nodeInfoArray.add(nodeInfo);
           for (int i=0;i<findNodes.size();i++) {
               for(int j=0;j<jNode.length;j++)
               {
               	if(findNodes.get(i).getNodeId().equals(jNode[j].getNode().getNodeId()))
               {
               	 FindNoderoads(j, val1,nodeInfoArray,number);
               }
               }
           }
//           System.out.println(jNode[0].getRoutingTable().getAllNodes());
//           System.out.print(lookup.routeLength());	
           }
    }
}

