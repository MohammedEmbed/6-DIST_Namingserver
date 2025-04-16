package kaasenwijn.namenode.util;

import kaasenwijn.namenode.service.NodeService;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class NodeMulticastReceiver extends Thread{
    private static final String multicastAddress = "230.0.0.0";
    private static final int PORT = 4446;

    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(multicastAddress);

            socket.joinGroup(group); // Join the multicast group
            socket.setTimeToLive(64); // Optionally set the time-to-live for multicast packets

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            System.out.println("Started listening on mutlticast: "+multicastAddress+":"+PORT);
            while (true) {
                socket.receive(packet); // Wait for multicast message

                String messageReceived = new String(packet.getData(), 0, packet.getLength());
                packet.setLength(buf.length); // Reset the length of the packet before the next packet is received

                // Process the message
                JSONObject obj = new JSONObject(messageReceived);

                // First check if it shouldn't be dropped, before processing further
                if(NodeService.shouldDrop(obj)){
                    System.out.println("[dropped] source and destination are the same");
                    continue;
                }

                String type = obj.getString("type");
                // Extract name & IP, Port
                JSONObject source = obj.getJSONObject("source");
                String name = source.getString("name");
                String ip = source.getString("ip");
                int port = source.getInt("port");


                switch (type){
                    case "bootstrap":
                        System.out.println("[bootstrap] "+ ip+":"+port+" ("+name+")");
                        int hashSender = NodeService.getHash(name);
                        JSONObject data = NodeService.updateNeighborsData(name, hashSender);
                        // Send back via unicast
                        if(!data.isEmpty()){
                            NodeSender.sendUnicastMessage(ip,port, "update_ids",data);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
