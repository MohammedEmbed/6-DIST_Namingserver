package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.FileRepository;
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
    private static final FileRepository fileRepository = FileRepository.getInstance();// filename → ownerIp


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
                    case "replication":
                        int nodeHash = data.getInt("nodeHash");      // hash of the node that sent the replication
                        int fileHash = data.getInt("fileHash");
                        String senderIp = source.getString("ip");    // IP of the sender (originating node)
                        int senderPort = source.getInt("port");
                        System.out.printf("[replication] Received unicast from %s: %s (hash=%d), nodeHash=%d%n", senderIp, senderPort, fileHash, nodeHash);

                        int ownerId = NameService.getFileOwnerId(fileHash, nodeHash);// Where it needs to replicate to
                        String ownerIpAndPort = IpRepository.getInstance().getIp(ownerId);// Ip of the owner (target)
                        String[] parts = ownerIpAndPort.split(":");
                        String ownerIp = parts[0];
                        int ownerPort = Integer.parseInt(parts[1]);
                        if (!ownerIp.equals(senderIp)) {
                            System.out.printf("[replication] Node %s should replicate '%s' to new owner %s", senderIp, fileHash,ownerIp);

                            //fileownership hashset is now persistant --> FIXED
                            fileRepository.register(fileHash, ownerIp);

                            // Tell sender to send the file to the actual owner
                            JSONObject resData = new JSONObject();
                            resData.put("fileHash",fileHash);
                            resData.put("ownerIp",ownerIp);
                            resData.put("ownerPort",ownerPort);
                            NameServerSender.sendUnicastMessage(senderIp, senderPort, "replication_response", resData);
                        } else {
                            System.out.printf("[replication] File '%s' is already owned by the reporting node %s%n",fileHash, senderIp);
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
