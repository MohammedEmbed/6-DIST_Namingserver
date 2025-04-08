package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.IpRepository;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class NameServerMulticastListener extends Thread {
    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private static final int MULTICAST_PORT = 4446;

    private static final int UNICAST_RECEIVE_PORT = 9090; // TCP port for receiving unicast
    private static final int UNICAST_SEND_PORT = 8081;    // TCP port for sending messages back to nodes

    private final NameService nameService;
    private final IpRepository ipRepo;

    public NameServerMulticastListener(NameService nameService) {
        this.nameService = nameService;
        this.ipRepo = IpRepository.getInstance();
    }

    public void run() {
        // Start TCP unicast listener in separate thread
        new Thread(this::runUnicastListener).start();
        // Run multicast listener on this thread
        runMulticastListener();
    }

    private void runMulticastListener(){
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

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

                // Send back via unicast
                sendNodeCountResponse(ip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send the node count back to the node
    private void sendNodeCountResponse(String ip) {
        int totalNodeCount = ipRepo.getMap().size();
        try (Socket responseSocket = new Socket(ip, UNICAST_SEND_PORT)) {
            OutputStream out = responseSocket.getOutputStream();
            String response = "{\"type\":\"welcome\", \"nodes\":" + totalNodeCount + "}";
            out.write(response.getBytes());
            out.flush();
            System.out.println("Sent node count (" + totalNodeCount + ") to: " + ip);
        } catch (Exception e) {
            System.err.println("Failed to send node count to: " + ip);
        }
    }

    private void runUnicastListener() {
        try (ServerSocket serverSocket = new ServerSocket(UNICAST_RECEIVE_PORT)) { //Verander de poort naar de gene die we willen gebruiken
            System.out.println("Unicast listener started on port 9090...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }

                JSONObject msg = new JSONObject(sb.toString());
                int nodeHash = msg.getInt("nodeHash");
                int fileHash = msg.getInt("fileHash");
                String filename = msg.getString("filename");
                String nodeIp = msg.getString("ip");

                System.out.printf("Received unicast from %s: %s (hash=%d), nodeHash=%d%n",
                        nodeIp, filename, fileHash, nodeHash);

                if (nodeHash < fileHash) {
                    System.out.printf("Node is a replicated one:", nodeIp, nodeHash, filename);
                    // TODO: Store this info, or trigger replication
                } else {
                    System.out.printf("No replication of this node:", nodeIp, filename);
                }

                in.close();
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
