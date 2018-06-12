package kademlia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import kademlia.exceptions.KadServerDownException;
import kademlia.message.KademliaMessageFactory;
import kademlia.message.Message;
import kademlia.message.MessageFactory;
import kademlia.node.Node;
import kademlia.message.Receiver;


public class KadServer
{

    
    private static final int DATAGRAM_BUFFER_SIZE = 64 * 1024;      

    
    private final transient KadConfiguration config;

    
    private final DatagramSocket socket;
    private transient boolean isRunning;
    private final Map<Integer, Receiver> receivers;
    private final Timer timer;      
    private final Map<Integer, TimerTask> tasks;    

    private final Node localNode;

    
    private final KademliaMessageFactory messageFactory;

    private final KadStatistician statistician;

    
    {
        isRunning = true;
        this.tasks = new HashMap<>();
        this.receivers = new HashMap<>();
        this.timer = new Timer(true);
    }

    
    public KadServer(int udpPort, KademliaMessageFactory mFactory, Node localNode, KadConfiguration config, KadStatistician statistician) throws SocketException
    {
        this.config = config;
        this.socket = new DatagramSocket(udpPort);
        this.localNode = localNode;
        this.messageFactory = mFactory;
        this.statistician = statistician;

        
        this.startListener();
    }

    
    private void startListener()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                listen();
            }
        }.start();
    }

    
    public synchronized int sendMessage(Node to, Message msg, Receiver recv) throws IOException, KadServerDownException
    {
        if (!isRunning)
        {
            throw new KadServerDownException(this.localNode + " - Kad Server is not running.");
        }

        
        int comm = new Random().nextInt();

        
        if (recv != null)
        {
            try
            {
                
                receivers.put(comm, recv);
                TimerTask task = new TimeoutTask(comm, recv);
                timer.schedule(task, this.config.responseTimeout());
                tasks.put(comm, task);
            }
            catch (IllegalStateException ex)
            {
                
            }
        }

        
        sendMessage(to, msg, comm);

        return comm;
    }

    
    public synchronized void reply(Node to, Message msg, int comm) throws IOException
    {
        if (!isRunning)
        {
            throw new IllegalStateException("Kad Server is not running.");
        }
        sendMessage(to, msg, comm);
    }

    
    private void sendMessage(Node to, Message msg, int comm) throws IOException
    {
        
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); DataOutputStream dout = new DataOutputStream(bout);)
        {
            
            dout.writeInt(comm);
            dout.writeByte(msg.code());
            msg.toStream(dout);
            dout.close();

            byte[] data = bout.toByteArray();

            if (data.length > DATAGRAM_BUFFER_SIZE)
            {
                throw new IOException("Message is too big");
            }

            
            DatagramPacket pkt = new DatagramPacket(data, 0, data.length);
            pkt.setSocketAddress(to.getSocketAddress());
            socket.send(pkt);

            
            this.statistician.sentData(data.length);
        }
    }

    
    private void listen()
    {
        try
        {
            while (isRunning)
            {
                try
                {
                    
                    byte[] buffer = new byte[DATAGRAM_BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    
                    this.statistician.receivedData(packet.getLength());

                    if (this.config.isTesting())
                    {
                        
                        int pause = packet.getLength() / 100;
                        try
                        {
                            Thread.sleep(pause);
                        }
                        catch (InterruptedException ex)
                        {

                        }
                    }

                    
                    try (ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                            DataInputStream din = new DataInputStream(bin);)
                    {

                        
                        int comm = din.readInt();
                        byte messCode = din.readByte();

                        Message msg = messageFactory.createMessage(messCode, din);
                        din.close();

                        
                        Receiver receiver;
                        if (this.receivers.containsKey(comm))
                        {
                            
                            synchronized (this)
                            {
                                receiver = this.receivers.remove(comm);
                                TimerTask task = (TimerTask) tasks.remove(comm);
                                if (task != null)
                                {
                                    task.cancel();
                                }
                            }
                        }
                        else
                        {
                            
                            receiver = messageFactory.createReceiver(messCode, this);
                        }

                        
                        if (receiver != null)
                        {
                            receiver.receive(msg, comm);
                        }
                    }
                }
                catch (IOException e)
                {
                    
                    System.err.println("Server ran into a problem in listener method. Message: " + e.getMessage());
                }
            }
        }
        finally
        {
            if (!socket.isClosed())
            {
                socket.close();
            }
            this.isRunning = false;
        }
    }

    
    private synchronized void unregister(int comm)
    {
        receivers.remove(comm);
        this.tasks.remove(comm);
    }

    
    public synchronized void shutdown()
    {
        this.isRunning = false;
        this.socket.close();
        timer.cancel();
    }

    
    class TimeoutTask extends TimerTask
    {

        private final int comm;
        private final Receiver recv;

        public TimeoutTask(int comm, Receiver recv)
        {
            this.comm = comm;
            this.recv = recv;
        }

        @Override
        public void run()
        {
            if (!KadServer.this.isRunning)
            {
                return;
            }

            try
            {
                unregister(comm);
                recv.timeout(comm);
            }
            catch (IOException e)
            {
                System.err.println("Cannot unregister a receiver. Message: " + e.getMessage());
            }
        }
    }

    public void printReceivers()
    {
        for (Integer r : this.receivers.keySet())
        {
            System.out.println("Receiver for comm: " + r + "; Receiver: " + this.receivers.get(r));
        }
    }

    public boolean isRunning()
    {
        return this.isRunning;
    }

}
