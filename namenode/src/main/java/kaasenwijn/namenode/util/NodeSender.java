package kaasenwijn.namenode.util;

import java.io.*;
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


    public static void sendFile(String ip, int port, String filename) throws IOException {
        Socket socket = new Socket(ip, port);
        File myFile = new File("local_files_"+NodeRepository.getInstance().getName()+"/" + filename);
        if (!myFile.exists()) {
            System.out.println("File doesn't exist!");
        } else {
            // First send the name
            JSONObject fileData = new JSONObject();
            fileData.put("fileName",filename);
            JSONObject unicastMessageObj = createObject("file_replication", fileData);

            OutputStream out = socket.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

            writer.write(unicastMessageObj.toString());
            writer.newLine();
            writer.write("END"); // End of JSON
            writer.newLine();
            writer.flush();

            // Then send the file
            FileInputStream fis = new FileInputStream(myFile);
            BufferedOutputStream bos = new BufferedOutputStream(out);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            fis.close();
            bos.flush();
            bos.close();
            System.out.println("File sent successfully!");
        }
    }

}




