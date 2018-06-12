package kademlia;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import kademlia.dht.GetParameter;
import kademlia.dht.DHT;
import kademlia.dht.KadContent;
import kademlia.dht.KademliaDHT;
import kademlia.dht.KademliaStorageEntry;
import kademlia.dht.JKademliaStorageEntry;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.exceptions.RoutingException;
import kademlia.message.MessageFactory;
import kademlia.node.Node;
import kademlia.node.KademliaId;
import kademlia.node.KeyComparator2;
import kademlia.operation.ConnectOperation;
import kademlia.operation.ContentLookupOperation;
import kademlia.operation.Operation;
import kademlia.operation.KadRefreshOperation;
import kademlia.operation.NodeLookupOperation;
import kademlia.operation.StoreOperation;
import kademlia.routing.JKademliaRoutingTable;
import kademlia.routing.KademliaRoutingTable;
import kademlia.util.serializer.JsonDHTSerializer;
import kademlia.util.serializer.JsonRoutingTableSerializer;
import kademlia.util.serializer.JsonSerializer;


public class JKademliaNode implements KademliaNode
{

    
    private final String ownerId;

    
    private final transient Node localNode;
    private final transient KadServer server;
    private final transient KademliaDHT dht;
    private transient KademliaRoutingTable routingTable;
    private final int udpPort;
    private transient KadConfiguration config;

    
    private transient Timer refreshOperationTimer;
    private transient TimerTask refreshOperationTTask;

    
    private final transient MessageFactory messageFactory;

    
    private final transient KadStatistician statistician;

    
    {
        statistician = new Statistician();
    }

    
    public JKademliaNode(String ownerId, Node localNode, int udpPort, KademliaDHT dht, KademliaRoutingTable routingTable, KadConfiguration config) throws IOException
    {
        this.ownerId = ownerId;
        this.udpPort = udpPort;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
        this.routingTable = routingTable;
        this.messageFactory = new MessageFactory(this, this.dht, this.config);
        this.server = new KadServer(udpPort, this.messageFactory, this.localNode, this.config, this.statistician);
        this.startRefreshOperation();
    }
    public void findNode(KademliaNode val) {
    	
    	 BigInteger b1 = val.getNode().getNodeId().getInt();
         b1.xor(this.getNode().getNodeId().getInt());
         System.out.println("所找节点ID为："+this.getNode().getNodeId()+"   节点距离为："+b1+"\n");
         int a = (int) Math.log(b1.doubleValue());
         System.out.println("k桶"+a+"内的信息为"+this.getRoutingTable().getBuckets()[a].getContacts());
         while(true) {
         if(this.getRoutingTable().getBuckets()[a].getNode().contains(val.getNode())) {
         	System.out.println(val.getOwnerId()+" "+val.getPort());
         	break;
         }
         else {
         NodeLookupOperation lookup = new NodeLookupOperation(this.getServer(),this,val.getNode().getNodeId(),new DefaultConfiguration(),new KeyComparator2(val.getNode().getNodeId()));
         try {
			lookup.execute();
		} catch (RoutingException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
         for (int i=0;i<lookup.Nodes(null).size();i++) {
         	System.out.print(lookup.Nodes(null).get(i).getNodeId()+"\n");	
         }
         for (int i=0;i<lookup.Nodes(null).size();i++) {
          	
          }
         System.out.print(lookup.routeLength());	
         }
         }
    }
    @Override
    public final void startRefreshOperation()
    {
        this.refreshOperationTimer = new Timer(true);
        refreshOperationTTask = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    
                    JKademliaNode.this.refresh();
                }
                catch (IOException e)
                {
                    System.err.println("KademliaNode: Refresh Operation Failed; Message: " + e.getMessage());
                }
            }
        };
        refreshOperationTimer.schedule(refreshOperationTTask, this.config.restoreInterval(), this.config.restoreInterval());
    }

    @Override
    public final void stopRefreshOperation()
    {
        
        this.refreshOperationTTask.cancel();
        this.refreshOperationTimer.cancel();
        this.refreshOperationTimer.purge();
    }

    public JKademliaNode(String ownerId, Node node, int udpPort, KademliaRoutingTable routingTable, KadConfiguration config) throws IOException
    {
        this(
                ownerId,
                node,
                udpPort,
                new DHT(ownerId, config),
                routingTable,
                config
        );
    }

    public JKademliaNode(String ownerId, Node node, int udpPort, KadConfiguration config) throws IOException
    {
        this(
                ownerId,
                node,
                udpPort,
                new JKademliaRoutingTable(node, config),
                config
        );
    }

    public JKademliaNode(String ownerId, KademliaId defaultId, int udpPort) throws IOException
    {
        this(
                ownerId,
                new Node(defaultId,InetAddress.getLocalHost(), udpPort),
                udpPort,
                new DefaultConfiguration()
        );
    }

    
    public static JKademliaNode loadFromFile(String ownerId) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        return JKademliaNode.loadFromFile(ownerId, new DefaultConfiguration());
    }

    
    public static JKademliaNode loadFromFile(String ownerId, KadConfiguration iconfig) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        DataInputStream din;

        
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "kad.kns"));
        JKademliaNode ikad = new JsonSerializer<JKademliaNode>().read(din);

        
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "routingtable.kns"));
        KademliaRoutingTable irtbl = new JsonRoutingTableSerializer(iconfig).read(din);

        
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "node.kns"));
        Node inode = new JsonSerializer<Node>().read(din);

        
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "dht.kns"));
        KademliaDHT idht = new JsonDHTSerializer().read(din);
        idht.setConfiguration(iconfig);

        return new JKademliaNode(ownerId, inode, ikad.getPort(), idht, irtbl, iconfig);
    }

    @Override
    public Node getNode()
    {
        return this.localNode;
    }

    @Override
    public KadServer getServer()
    {
        return this.server;
    }

    @Override
    public KademliaDHT getDHT()
    {
        return this.dht;
    }

    @Override
    public KadConfiguration getCurrentConfiguration()
    {
        return this.config;
    }

    @Override
    public synchronized final void bootstrap(Node n) throws IOException, RoutingException
    {
        long startTime = System.nanoTime();
        Operation op = new ConnectOperation(this.server, this, n, this.config);
        op.execute();
        long endTime = System.nanoTime();
        this.statistician.setBootstrapTime(endTime - startTime);
    }

    @Override
    public int put(KadContent content) throws IOException
    {
        return this.put(new JKademliaStorageEntry(content));
    }

    @Override
    public int put(JKademliaStorageEntry entry) throws IOException
    {
        StoreOperation sop = new StoreOperation(this.server, this, entry, this.dht, this.config);
        sop.execute();

        
        return sop.numNodesStoredAt();
    }

    @Override
    public void putLocally(KadContent content) throws IOException
    {
        this.dht.store(new JKademliaStorageEntry(content));
    }

    @Override
    public JKademliaStorageEntry get(GetParameter param) throws NoSuchElementException, IOException, ContentNotFoundException
    {
        if (this.dht.contains(param))
        {
            
            return this.dht.get(param);
        }

        
        long startTime = System.nanoTime();
        ContentLookupOperation clo = new ContentLookupOperation(server, this, param, this.config);
        clo.execute();
        long endTime = System.nanoTime();
        this.statistician.addContentLookup(endTime - startTime, clo.routeLength(), clo.isContentFound());
        return clo.getContentFound();
    }

    @Override
    public void refresh() throws IOException
    {
        new KadRefreshOperation(this.server, this, this.dht, this.config).execute();
    }

    @Override
    public String getOwnerId()
    {
        return this.ownerId;
    }

    @Override
    public int getPort()
    {
        return this.udpPort;
    }

    @Override
    public void shutdown(final boolean saveState) throws IOException
    {
        
        this.server.shutdown();

        this.stopRefreshOperation();

        
        if (saveState)
        {
            
            this.saveKadState();
        }
    }

    @Override
    public void saveKadState() throws IOException
    {
        DataOutputStream dout;

        
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "kad.kns"));
        new JsonSerializer<JKademliaNode>().write(this, dout);

        
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "node.kns"));
        new JsonSerializer<Node>().write(this.localNode, dout);

        
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "routingtable.kns"));
        new JsonRoutingTableSerializer(this.config).write(this.getRoutingTable(), dout);

        
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "dht.kns"));
        new JsonDHTSerializer().write(this.dht, dout);

    }

    
    private static String getStateStorageFolderName(String ownerId, KadConfiguration iconfig)
    {
        
        String path = iconfig.getNodeDataFolder(ownerId) + File.separator + "nodeState";
        File nodeStateFolder = new File(path);
        if (!nodeStateFolder.isDirectory())
        {
            nodeStateFolder.mkdir();
        }
        return nodeStateFolder.toString();
    }

    @Override
    public KademliaRoutingTable getRoutingTable()
    {
        return this.routingTable;
    }

    @Override
    public KadStatistician getStatistician()
    {
        return this.statistician;
    }

    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("\n\nPrinting Kad State for instance with owner: ");
        sb.append(this.ownerId);
        sb.append("\n\n");

        sb.append("\n");
        sb.append("Local Node");
        sb.append(this.localNode);
        sb.append("\n");

        sb.append("\n");
        sb.append("Routing Table: ");
        sb.append(this.getRoutingTable());
        sb.append("\n");

        sb.append("\n");
        sb.append("DHT: ");
        sb.append(this.dht);
        sb.append("\n");

        sb.append("\n\n\n");

        return sb.toString();
    }
}
