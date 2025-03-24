package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.model.Node;
import kaasenwijn.namingserver.repository.IpRepository;
import kaasenwijn.namingserver.service.NameService;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

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
                socket.receive(packet);// Receive packet from multicast group

                String messageReceived = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Multicast received: " + messageReceived);
                // Process the message
                JSONObject obj = new JSONObject(messageReceived);
                if ("bootstrap".equals(obj.getString("type"))) {
                    // Extract node name and IP
                    String name = obj.getString("name");
                    String ip = obj.getString("ip");

                    // Compute hash ID for the node and store it in the IP map
                    Integer id = nameService.getHash(name);
                    ipRepo.setIp(id, ip);

                    System.out.println("Added node from multicast: " + name + " (" + ip + ")");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
