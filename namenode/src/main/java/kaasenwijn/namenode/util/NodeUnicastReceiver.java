package kaasenwijn.namenode.util;

import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.NodeService;
import org.json.JSONObject;

import java.io.*;
import java.net.*;

public class NodeUnicastReceiver extends Thread {
    private static final int UNICAST_SENDER_PORT = 9090; // Node unicast sender port = flipped t.o.v. nameServer

    private static final NodeRepository nodeRepository = NodeRepository.getInstance();

    @Override
    public void run() {
        try {
            InetAddress bindAddress = InetAddress.getByName(nodeRepository.getSelfIp());
            ServerSocket serverSocket = new ServerSocket(nodeRepository.getSelfPort(), 50, bindAddress);
            System.out.println("Socked opened on: " + nodeRepository.getSelfIp() + ":" + nodeRepository.getSelfPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                StringBuilder messageBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    messageBuilder.append(line);
                }

                String message = messageBuilder.toString();
                JSONObject json = new JSONObject(message);
                String type = json.getString("type");
                // Contains ip, port and name of sender
                JSONObject source = json.getJSONObject("source");
                JSONObject data = json.getJSONObject("data");

                switch (type) {
                    case "health-check":
                        // We just want to test if the node is alive, we don't need to respond explicitly
                        break;
                    case "welcome":
                        int nodeCount = data.getInt("nodes");
                        System.out.println("[Welcome] Nodes in system: " + nodeCount);

                        // If count is one, the node is alone in the network
                        if (nodeCount == 1) {
                            nodeRepository.setPrevious(nodeRepository.getCurrentId());
                            nodeRepository.setNext(nodeRepository.getCurrentId());
                        } else {
                            nodeRepository.setPrevious(data.getInt("previousNode"));
                            nodeRepository.setNext(data.getInt("nextNode"));
                        }
                        System.out.println("[welcome] nextid: " + nodeRepository.getNextId() + " , previousid: " + nodeRepository.getPreviousId());

                        // If the count is not 1, it wil receive messages from other nodes to update prev and next id

                        // TODO: is this for lab5?
                        // TODO: Replace with actual IP
                        // Reply with local file report
                        //sendFileReportViaTCP("<NAMING_SERVER_IP>", UNICAST_SENDER_PORT);
                        break;

                    case "update_ids":
                        if (!data.isEmpty()) {
                            int nextId = data.getInt("next_id");
                            int previousId = data.getInt("previous_id");
                            nodeRepository.setNext(nextId);
                            nodeRepository.setPrevious(previousId);
                            System.out.println("[update_ids] New nextid: " + nextId + " , new previousid: " + previousId);
                        }
                        break;

                    // For Shutdown
                    case "update_next_id":
                        if (!data.isEmpty()) {
                            int nextId = data.getInt("next_id");
                            nodeRepository.setNext(nextId);
                            System.out.println("[update_next_id] New nextid: " + nextId);
                        }
                        break;

                    case "update_previous_id":
                        if (!data.isEmpty()) {
                            int previousId = data.getInt("previous_id");
                            nodeRepository.setPrevious(previousId);
                            System.out.println("[update_previous_id] New previousid: " + previousId);
                        }
                        break;

                    // TODO: Get file name from HashSet 'knownfiles' (fileMonitor)
                    //
                    case "replication_response":
                        String filename = data.getString("fileHash");
                        String targetIp = source.getString("ip");
                        int targetPort = source.getInt("port");
                        System.out.printf("Received replication response → Send '%s' to %s%n", filename, targetIp);
                        NodeSender.sendFileToNode(filename, targetIp, targetPort);
                        break;

                    case "file_replication": //The node RECEIVES a file from another node to be replicated on it.
                        //TODO Get the file, save and log it

                }

                in.close();
                clientSocket.close();
            }

        } catch (IOException e) {
            System.err.println("Error in UnicastReceiver: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
