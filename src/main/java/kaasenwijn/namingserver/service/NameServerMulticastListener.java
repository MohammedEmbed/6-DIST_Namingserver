package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.IpRepository;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class NameServerMulticastListener extends Thread {
    String multicastAddress = "230.0.0.0";
    private static final int PORT = 4446;
    private final NameService nameService;
    private final IpRepository ipRepo;

    public NameServerMulticastListener(NameService nameService) {
        this.nameService = nameService;
        this.ipRepo = IpRepository.getInstance();
    }

    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(multicastAddress);

            socket.joinGroup(group); // Join the multicast group
            socket.setTimeToLive(64); // Optionally set the time-to-live for multicast packets

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (true) {
                socket.receive(packet); // Wait for multicast message

                String messageReceived = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Multicast received: " + messageReceived);
                packet.setLength(buf.length); // Reset the length of the packet before the next packet is received

                // Process the message
                JSONObject obj = new JSONObject(messageReceived);

                // Extract name & IP, compute hash
                String name = obj.getString("name");
                String ip = obj.getString("ip");
                int nodeId = nameService.getHash(name);

                // Store (hash, IP) in naming server map
                ipRepo.setIp(nodeId, ip);
                System.out.println("Added node from multicast: " + name + " (" + ip + ") → ID: " + nodeId);

                // Respond to the node with the total number of nodes
                int totalNodeCount = ipRepo.getMap().size();

                try (Socket responseSocket = new Socket(ip, 8081)) { // node must listen here
                    OutputStream out = responseSocket.getOutputStream();
                    String response = "{\"nodes\":" + totalNodeCount + "}";
                    out.write(response.getBytes());
                    out.flush();
                    System.out.println("Sent node count (" + totalNodeCount + ") to: " + ip);
                } catch (Exception e) {
                    System.err.println("Unable to respond to new node at: " + ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
