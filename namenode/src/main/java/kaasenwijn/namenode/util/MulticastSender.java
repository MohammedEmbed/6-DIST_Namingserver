package kaasenwijn.namenode.util;

import java.io.OutputStream;
import java.net.*;

import org.json.JSONObject;

public class MulticastSender {
    private static final String MULTICAST_ADDRESS = "230.0.0.0"; // multicast group address
    private static final int PORT = 4446; // port on which to send

    private static final int UNICAST_SEND_PORT = 9090; // TCP port for sending unicast (to the NameServer)

    String type = "bootstrap";

    public static void sendMulticastMessage(String name, String ip) {
        try (DatagramSocket socket = new DatagramSocket()) { // creates a UDP socket

            JSONObject multicastMessageObj = createObject(name, ip);
            byte[] buf = multicastMessageObj.toString().getBytes(); // the message we want to send turned into bytes

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet); // sends the UDP packet to the multicast group

            System.out.println("Multicast message sent to group: " + MULTICAST_ADDRESS + ":" + PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendUnicastMessage(String name, String ip) {
        final String NAMING_SERVER_IP = "<NAMING_SERVER_IP>";

        try(Socket socket = new Socket(NAMING_SERVER_IP, UNICAST_SEND_PORT)){
            System.out.println("Unicast send started on port 9090...");

            JSONObject unicastMessageObj = createObject(name, ip);
            byte[] buf = unicastMessageObj.toString().getBytes(); // the message we want to send turned into bytes

            OutputStream out = socket.getOutputStream();
            out.write(buf);
            out.flush();

            System.out.println(" Unicast bootstrap message sent.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static JSONObject createObject(String name, String ip){
        // Create JSON message with name and ip
        JSONObject messageObj = new JSONObject();
        messageObj.put("type", "bootstrap");
        messageObj.put("name", name);
        messageObj.put("ip", ip);

        return messageObj;
    }

    private static JSONObject createObject(String type, String name, String ip){ //Method overload
        // Create JSON message with name and ip
        JSONObject messageObj = new JSONObject();
        messageObj.put("type", type);
        messageObj.put("name", name);
        messageObj.put("ip", ip);

        return messageObj;
    }
}
