package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.IpRepository;
import kaasenwijn.namingserver.repository.NodeRepository;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class NameServerUnicastReceiver extends Thread {

    private static final NodeRepository nodeRepository = NodeRepository.getInstance();
    private static final HashMap<String, String> fileOwnership = new HashMap<>(); // filename → ownerIp
    private static final String LOG_FILE = "replication_log.txt";

    @Override
    public void run() {
        try  {
            InetAddress bindAddress = InetAddress.getByName(nodeRepository.getSelfIp());
            ServerSocket serverSocket = new ServerSocket(nodeRepository.getSelfPort(),50,bindAddress);
            System.out.println("Socket opened on: "+nodeRepository.getSelfIp()+":"+nodeRepository.getSelfPort());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }

                String message = sb.toString();
                JSONObject json = new JSONObject(message);
                String type = json.getString("type");
                JSONObject data = json.getJSONObject("data");
                JSONObject source = json.getJSONObject("source");

                switch (type){
                    // TODO: lab 5
                    case "replication":
                        int nodeHash = data.getInt("nodeHash");      // hash of the node that sent the replication
                        int fileHash = data.getInt("fileHash");      // hash of the file
                        String filename = data.getString("filename");
                        String senderIp = source.getString("ip");    // IP of the sender (originating node)

                        System.out.printf("Received unicast from %s: %s (hash=%d), nodeHash=%d%n",
                                senderIp, filename, fileHash, nodeHash);

                        if (nodeHash < fileHash) {
                            System.out.printf("Node is a replicated one:", senderIp, nodeHash, filename);

                            int ownerId = NameService.getNodeId(filename);
                            String targetIp = IpRepository.getInstance().getIp(ownerId);

                            if (!targetIp.equals(senderIp)) {
                                System.out.printf("Node %s should replicate '%s' to new owner %s%n", senderIp, filename, targetIp);

                                fileOwnership.put(filename, targetIp);
                                logReplication(filename, targetIp);

                                // Tell sender to send the file to the actual owner
                                NameServerSender.unicastSend(senderIp, targetIp, filename);
                            } else {
                                System.out.printf("File '%s' is already owned by the reporting node %s%n", filename, senderIp);
                            }

                        } else {
                            System.out.printf("No replication of this node:", senderIp, filename);
                        }
                        break;
                }
                in.close();
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create a Log with information about on the file that's replicated
    private void logReplication(String filename, String nodeIp) {
        try (FileWriter fileWriter = new FileWriter(LOG_FILE, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter out = new PrintWriter(bufferedWriter)) {

            String logEntry = String.format("Replicated file: %s → Owner: %s", filename, nodeIp);
            out.println(logEntry);
            System.out.println("Logged replication: " + logEntry);

        } catch (IOException e) {
            System.err.println("Failed to log replication.");
            e.printStackTrace();
        }
    }
}
