package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.IpRepository;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class NameServerMulticastReceiver extends Thread {
    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private static final int MULTICAST_PORT = 4446;
    private final IpRepository ipRepo = IpRepository.getInstance();

    public void run() {
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            socket.joinGroup(group); // Join the multicast group
            socket.setTimeToLive(64); // Optionally set the time-to-live for multicast packets

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (true) {
                socket.receive(packet); // Wait for multicast message

                String messageReceived = new String(packet.getData(), 0, packet.getLength());
                packet.setLength(buf.length); // Reset the length of the packet before the next packet is received

                // Process the message
                JSONObject obj = new JSONObject(messageReceived);
                String type = obj.getString("type");
                JSONObject source = obj.getJSONObject("source");

                // Extract name & IP, compute hash
                String name = source.getString("name");
                String ip = source.getString("ip");
                int port = source.getInt("port");

                switch (type) {
                    case "bootstrap":
                        System.out.println("[bootstrap 1/2] Received bootstrap multicast from: " + ip + ":" + port + " (" + name + ")");
                        // Store (hash, IP) in naming server map
                        int nodeId = NameService.getHash(name);
                        String toStoreIp = ip + ':' + port;
                        ipRepo.setIp(nodeId, toStoreIp);
                        System.out.println("[bootstrap 2/2] Added node from multicast: " + name + " (" + ip + ") → ID: " + nodeId);
                        // Send back via unicast
                        JSONObject data = new JSONObject();
                        int systemSize = ipRepo.getMap().size();
                        data.put("nodes", systemSize);
                        if (systemSize == 1) {
                            data.put("previousNode", nodeId);
                            data.put("nextNode", nodeId);
                        } else {
                            data.put("previousNode", ipRepo.getPreviousId(nodeId));
                            data.put("nextNode", ipRepo.getNextId(nodeId));
                        }

                        NameServerSender.sendUnicastMessage(ip, port, "welcome", data);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

