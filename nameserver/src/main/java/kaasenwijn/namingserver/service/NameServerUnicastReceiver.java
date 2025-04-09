package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.NodeRepository;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NameServerUnicastReceiver extends Thread {
    private static final NodeRepository nodeRepository = NodeRepository.getInstance();
    @Override
    public void run() {
        try  {
            InetAddress bindAddress = InetAddress.getByName(nodeRepository.getSelfIp());
            ServerSocket serverSocket = new ServerSocket(nodeRepository.getSelfPort(),50,bindAddress);
            System.out.println("Socked opened on: "+nodeRepository.getSelfIp()+":"+nodeRepository.getSelfPort());
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
                        int nodeHash = data.getInt("nodeHash");
                        int fileHash = data.getInt("fileHash");
                        String filename = data.getString("filename");
                        String nodeIp = source.getString("ip");
                        System.out.printf("Received unicast from %s: %s (hash=%d), nodeHash=%d%n",
                                nodeIp, filename, fileHash, nodeHash);
                        if (nodeHash < fileHash) {
                            System.out.printf("Node is a replicated one:", nodeIp, nodeHash, filename);
                            // TODO: Store this info, or trigger replication
                        } else {
                            System.out.printf("No replication of this node:", nodeIp, filename);
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
}
