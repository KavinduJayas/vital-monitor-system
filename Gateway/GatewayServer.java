import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class GatewayServer implements Runnable{

    private Monitor monitorCredential = null;
    private final Socket clientSocket = new Socket();

    //Converts a serialized byte-stream to a Monitor object
    private static Monitor parseFromByteArray(byte[] monitorInBytes) {

            Monitor monitorCredential = null;

            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(monitorInBytes);
                ObjectInputStream ois = new ObjectInputStream(bis);
                monitorCredential = (Monitor) ois.readObject();
            } catch (ClassNotFoundException e) {
                System.out.println("Class definition error: "+e.getMessage());
            } catch (IOException e){
                System.out.println("I/O error: " + e.getMessage());
            }

        return monitorCredential;
    }

    //creates a UDP socket at a given port
    private static DatagramSocket createDatagramSocket(int port){
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("Socket creation error: " + e.getMessage());;
        }
        return datagramSocket;

    }

    public static void main(String[] args){
       /*
        *  Main thread is used to listen to UPD broadcasts from the vital monitors,
        *  Once a message is received, it is parsed, and a new thread is created to handle the connection.
        *
        *  The monitor ids are recorded in an Arraylist when identified,
        *  A new thread is created for previously unseen monitor ids only.
        *  This process happens inside the main thread and therefore needs no explicit synchronization.
        *
        */

        int BROADCAST_LISTEN_PORT = 6000;
        int index=0;
        String monitorId;
        DatagramSocket datagramSocket = createDatagramSocket(BROADCAST_LISTEN_PORT);
        GatewayServer gateway = null;

        ArrayList<Thread> threadsList = new ArrayList<Thread>();
        ArrayList<String> monitorIds = new ArrayList<String>();

        //buffer for containing the byte-stream
        byte[] buffer = new byte[500];

        while(true) {
            try {
                gateway = new GatewayServer();
                DatagramPacket datagram = new DatagramPacket(buffer, 500);
                datagramSocket.receive(datagram);

                gateway.monitorCredential = parseFromByteArray(datagram.getData());

                //skip the current iteration if the byte-stream could not be properly parsed
                if(gateway.monitorCredential == null){
                    System.out.println("Parsing failed! \nresuming...");
                    continue;
                }
                monitorId = gateway.monitorCredential.getMonitorID();

                //create new thread for connection if the monitor is not already connected
                if(!monitorIds.contains(monitorId)) {
                    monitorIds.add(monitorId);
                    threadsList.add(new Thread(gateway));
                    threadsList.get(index).start();
                    System.out.println("thread started for handling the monitor: "+ monitorId
                            + " on socket: " + gateway.monitorCredential.getIp() + gateway.monitorCredential.getPort());
                    index++;
                }

            } catch (IOException e) {
                System.out.println("I/O error: " + e.getMessage());
            }
        }
    }

    //worker function
    @Override
    public void run() {
        try {
            InetSocketAddress inetSockAdd = new InetSocketAddress(this.monitorCredential.getIp(), this.monitorCredential.getPort());
            clientSocket.connect(inetSockAdd);
            InputStream inputStream = clientSocket.getInputStream();
            System.out.println("successfully connected to "+ inetSockAdd.toString());

            char character = (char) inputStream.read();
            StringBuilder message = new StringBuilder();

            // put the message in a buffer
            while(character != '\n'){
                message.append(character);
                character = (char) inputStream.read();
            }
            // display vital monitor message
            System.out.println(message);
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
