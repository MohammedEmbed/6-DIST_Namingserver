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
            ServerSocket serverSocket = new ServerSocket(nodeRepository.getSelfPort(),50,bindAddress);
            System.out.println("Socked opened on: "+nodeRepository.getSelfIp()+":"+nodeRepository.getSelfPort());

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

                switch (type){
                    case "health-check":
                        // We just want to test if the node is alive, we don't need to respond explicitly
                        break;
                    case "welcome":
                        int nodeCount = data.getInt("nodes");
                        System.out.println("[Welcome] Nodes in system: " + nodeCount);

                        // If count is one, the node is alone in the network
                        if (nodeCount == 1){
                            nodeRepository.setPrevious(nodeRepository.getCurrentId());
                            nodeRepository.setNext(nodeRepository.getCurrentId());
                        } else {
                            // TODO: remove placeholder with actual name of the node
                            // This data is not saved on the NS, so either the name has to be saved in the hashmap on NS
                            //  or the NS needs an API endpoint to retrieve the IP by hash instead of by name
                            nodeRepository.setPrevious(data.getInt("previousNode"));
                            nodeRepository.setNext(data.getInt("nextNode"));
                        }
                        System.out.println("[welcome] nextid: "+nodeRepository.getNextId()+" , previousid: "+nodeRepository.getPreviousId());

                        // If the count is not 1, it wil receive messages from other nodes to update prev and next id

                        // TODO: is this for lab5?
                        // TODO: Replace with actual IP
                        // Reply with local file report
                        //sendFileReportViaTCP("<NAMING_SERVER_IP>", UNICAST_SENDER_PORT);
                        break;

                    case "update_ids":
                        if(!data.isEmpty()){
                            int nextId = data.getInt("next_id");
                            int previousId = data.getInt("previous_id");
                            nodeRepository.setNext(nextId);
                            nodeRepository.setPrevious(previousId);
                            System.out.println("[update_ids] New nextid: "+nextId+" , new previousid: "+previousId);
                        }
                        break;

                    // For Shutdown
                    case "update_next_id":
                        if(!data.isEmpty()){
                            int nextId = data.getInt("next_id");
                            nodeRepository.setNext(nextId);
                            System.out.println("[update_next_id] New nextid: "+nextId);
                        }
                        break;

                    case "update_previous_id":
                        if (!data.isEmpty()){
                            int previousId = data.getInt("previous_id");
                            nodeRepository.setPrevious(previousId);
                            System.out.println("[update_previous_id] New previousid: "+previousId);
                        }
                    break;

                    // TODO: fix for lab5
                    case "replication_request":
                        String filename = data.getString("file");
                        String targetIp = data.getString("to");
                        System.out.printf("Received replication request → Send '%s' to %s%n", filename, targetIp);
                        sendFileToNode(filename, targetIp);
                        break;
                }

                in.close();
                clientSocket.close();
            }

        } catch (IOException e) {
            System.err.println("Error in UnicastReceiver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // TODO: fix for lab5
    // Sends file report to naming server via TCP
    private void sendFileReportViaTCP(String namingServerIp, int port) {
        try {
            File folder = new File("files");
            if (!folder.exists()) {
                System.out.println("Local files folder does not exist.");
                return;
            }

            JSONObject report = new JSONObject();
            report.put("ip", nodeRepository.getSelfIp());

            JSONObject files = new JSONObject();
            for (File file : folder.listFiles()) {
                if (!file.isFile()) continue;
                String filename = file.getName();
                int hash = NodeService.getHash(filename);
                files.put(filename, hash);
            }
            report.put("files", files);

            Socket socket = new Socket(namingServerIp, port);
            OutputStream out = socket.getOutputStream();
            out.write(report.toString().getBytes());
            out.flush();
            socket.close();

            System.out.println("File report sent to naming server via TCP.");

        } catch (Exception e) {
            System.err.println("Failed to send TCP report to naming server:");
            e.printStackTrace();
        }
    }

    // TODO: fix for lab5
    // Sends file to another node via HTTP POST
    private void sendFileToNode(String filename, String targetIp) {
        try {
            File file = new File("files", filename);
            if (!file.exists()) {
                System.err.println("File not found: " + filename);
                return;
            }

            URL url = new URL("http://" + targetIp + ":8080/api/node/files/replicate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("File-Name", filename);
            conn.setRequestProperty("Content-Type", "application/octet-stream");

            try (OutputStream os = conn.getOutputStream();
                 FileInputStream fis = new FileInputStream(file)) {
                fis.transferTo(os);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("File '" + filename + "' successfully sent to " + targetIp);
            } else {
                System.err.println("Failed to send file to " + targetIp + " — HTTP " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("Error sending file to " + targetIp);
            e.printStackTrace();
        }
    }
}
