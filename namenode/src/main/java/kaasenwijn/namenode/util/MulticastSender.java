package kaasenwijn.node.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.JSONObject;

public class MulticastSender {
    private static final String MULTICAST_ADDRESS = "230.0.0.0"; // multicast group address
    private static final int PORT = 4446; // port on which to send

    public static void sendMulticastMessage(String name, String ip) {
        try (DatagramSocket socket = new DatagramSocket()) { // creates a UDP socket
            // Create JSON message with name and ip
            JSONObject messageObj = new JSONObject();
            messageObj.put("type", "bootstrap");
            messageObj.put("name", name);
            messageObj.put("ip", ip);
            byte[] buf = messageObj.toString().getBytes(); // the message we want to send turned into bytes

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet); // sends the UDP packet to the multicast group

            System.out.println("Multicast message sent to group: " + MULTICAST_ADDRESS + ":" + PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
