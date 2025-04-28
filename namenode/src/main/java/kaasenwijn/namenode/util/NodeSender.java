package kaasenwijn.namenode.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.*;

import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.NodeService;
import org.json.JSONObject;

public class NodeSender {
    private static final String MULTICAST_ADDRESS = "230.0.0.0"; // multicast group address
    private static final int PORT = 4446; // port on which to send

    private static final NodeRepository nodeRepository = NodeRepository.getInstance();

    public static void sendMulticastMessage(String type) {
        try (DatagramSocket socket = new DatagramSocket()) { // creates a UDP socket
            // Create JSON message with name and ip
            JSONObject messageObj = createObject(type);
            byte[] buf = messageObj.toString().getBytes(); // the message we want to send turned into bytes
            socket.setBroadcast(true);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet); // sends the UDP packet to the multicast group

            System.out.println("Multicast message sent to group: " + MULTICAST_ADDRESS + ":" + PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendUnicastMessage(String ip, int port, String type, JSONObject data) throws CommunicationException {
        try (Socket socket = new Socket(ip, port)) {
            JSONObject unicastMessageObj = createObject(type, data);
            byte[] buf = unicastMessageObj.toString().getBytes(); // the message we want to send turned into bytes

            OutputStream out = socket.getOutputStream();
            out.write(buf);
            out.flush();

            System.out.println(" Unicast message sent: ip=" + ip + " ,port=" + port + " ,type=" + type);

        } catch (Exception e) {
            throw new CommunicationException(ip, port);
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

    // TODO: @Warre TCP
    // Sends file to another node via HTTP POST --> Might need to be changed since http is turned off for us
    protected static void sendFileToNode(String filename, String targetIp, int NODE_PORT) {
        try {
            File file = new File("files", filename);
            if (!file.exists()) {
                System.err.println("File not found: " + filename);
                return;
            }

            URL url = new URL("http://" + targetIp + ":" + NODE_PORT + "/api/node/files/"); //TODO
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

    public static void sendUnicastMessage(String ip, int port, String type) throws CommunicationException {
        sendUnicastMessage(ip, port, type, new JSONObject());
    }

    private static JSONObject createObject(String type) { //Method overload
        // Create JSON message with name and ip
        JSONObject messageObj = new JSONObject();
        messageObj.put("type", type);
        messageObj.put("source", createSourceObject());
        return messageObj;
    }

    private static JSONObject createObject(String type, JSONObject data) { //Method overload
        // Create JSON message with name and ip
        JSONObject messageObj = createObject(type);
        messageObj.put("data", data);
        return messageObj;
    }

    private static JSONObject createSourceObject() {
        // Create JSON message with name and ip
        JSONObject messageObj = new JSONObject();
        messageObj.put("name", nodeRepository.getName());
        messageObj.put("ip", nodeRepository.getSelfIp());
        messageObj.put("port", nodeRepository.getSelfPort());
        return messageObj;
    }
}

