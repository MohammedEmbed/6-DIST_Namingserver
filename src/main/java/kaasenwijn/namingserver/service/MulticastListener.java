package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.IpRepository;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class MulticastListener extends Thread {
    String multicastAddress = "230.0.0.0";
    private static final int PORT = 4446;
    private final NameService nameService;
    private final IpRepository ipRepo;

    public MulticastListener(NameService nameService) {
        this.nameService = nameService;
        this.ipRepo = IpRepository.getInstance();
    }

    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(multicastAddress);

            // Join the multicast group
            socket.joinGroup(group);

            // Optionally set the time-to-live for multicast packets
            socket.setTimeToLive(64);

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (!Thread.currentThread().isInterrupted()) {
                // Receive packet from multicast group
                socket.receive(packet);
                String messageReceived = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Multicast received: " + messageReceived);

                // Process the message to see if it's a bootstrap
                JSONObject obj = new JSONObject(messageReceived);
                if ("bootstrap".equals(obj.getString("type"))) {
                    // Extract node name and IP
                    String name = obj.getString("name");
                    String ip = obj.getString("ip");

                    // Compute hash ID for the node and store it in the IP map
                    int nodeId = nameService.getHash(name);
                    ipRepo.setIp(nodeId, ip);

                    System.out.println("Added node from multicast: " + name + " (" + ip + ")");

                    // Send response back to node with total count
                    int totalNodeCount = ipRepo.getMap().size();

                    try (Socket responseSocket = new Socket(ip, 8081)) { // node must listen here
                        OutputStream out = responseSocket.getOutputStream();
                        String response = "{\"type\":\"welcome\", \"nodes\":" + totalNodeCount + "}";
                        out.write(response.getBytes());
                        out.flush();
                        System.out.println("Sent node count (" + totalNodeCount + ") to: " + ip);
                    } catch (Exception e) {
                        System.err.println("Unable to respond to new node at: " + ip);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
