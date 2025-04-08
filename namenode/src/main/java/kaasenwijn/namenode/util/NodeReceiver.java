package kaasenwijn.namenode.util;

import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.NodeService;
import org.json.JSONObject;

import java.io.*;
<<<<<<< HEAD
import java.net.ServerSocket;
=======
        import java.net.ServerSocket;
>>>>>>> origin/Lab5
import java.net.Socket;
import java.net.HttpURLConnection;
import java.net.URL;

public class NodeReceiver extends Thread {

<<<<<<< HEAD
    private static final int UNICAST_SENDER_PORT = 9090; // Node unicast sender port = flipped t.o.v. nameServer
    private static final int UNICAST_RECEIVE_PORT = 8081; // Node unicast listener port


=======
>>>>>>> origin/Lab5
    private final int currentID;
    private final String selfIp;

    public NodeReceiver(String nodeName) {
        this.currentID = NodeService.getHash(nodeName);
        this.selfIp = NodeRepository.getInstance().getSelfIp();
    }

    @Override
    public void run() {
<<<<<<< HEAD
        try (ServerSocket serverSocket = new ServerSocket(UNICAST_RECEIVE_PORT)) {
            System.out.println("UnicastReceiver started on port 8081...");
=======
        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            System.out.println("📡 UnicastReceiver started on port 8081...");
>>>>>>> origin/Lab5

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                StringBuilder messageBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    messageBuilder.append(line);
                }

                String message = messageBuilder.toString();
                System.out.println("Received message: " + message);

                if (message.contains("welcome")) {
                    JSONObject welcome = new JSONObject(message);
                    int nodeCount = welcome.getInt("nodes");

                    System.out.println("Welcome from naming server. Nodes in system: " + nodeCount);

                    // Reply with local file report
<<<<<<< HEAD
                    sendFileReportViaTCP("<NAMING_SERVER_IP>", UNICAST_SENDER_PORT); // TODO: Replace with actual IP
=======
                    sendFileReportViaTCP("<NAMING_SERVER_IP>", 9090); // TODO: Replace with actual IP
>>>>>>> origin/Lab5
                }

                else if (message.contains("replication_request")) {
                    JSONObject request = new JSONObject(message);
                    String filename = request.getString("file");
                    String targetIp = request.getString("to");

                    System.out.printf("Received replication request → Send '%s' to %s%n", filename, targetIp);

                    sendFileToNode(filename, targetIp);
                }

                in.close();
                clientSocket.close();
            }

        } catch (IOException e) {
            System.err.println("Error in UnicastReceiver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Sends file report to naming server via TCP
    private void sendFileReportViaTCP(String namingServerIp, int port) {
        try {
            File folder = new File("files");
            if (!folder.exists()) {
                System.out.println("Local files folder does not exist.");
                return;
            }

            JSONObject report = new JSONObject();
            report.put("ip", selfIp);

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
