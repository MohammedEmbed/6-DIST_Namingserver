package kaasenwijn.namenode.service;

import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.NodeService;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.HttpURLConnection;
import java.net.URL;

public class UnicastReceiver extends Thread {

    private final int currentID;
    private final String selfIp;

    public UnicastReceiver(String nodeName) {
        this.currentID = NodeService.getHash(nodeName);
        this.selfIp = NodeRepository.getInstance().getSelfIp();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            System.out.println("UnicastReceiver started on port 8081...");

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

                    // Reply with local file list
                    sendFileReportViaTCP("<NAMING_SERVER_IP>", 9090); //TODO: Change IP to naming server
                }

                in.close();
                clientSocket.close();
            }

        } catch (IOException e) {
            System.err.println("Error in UnicastReceiver: " + e.getMessage());
        }
    }

    // Sends file report via HTTP POST
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
}
