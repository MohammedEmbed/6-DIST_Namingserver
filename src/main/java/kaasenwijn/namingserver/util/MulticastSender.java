package kaasenwijn.namingserver.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastSender {
    private static final String MULTICAST_ADDRESS = "230.0.0.0"; // multicast group address
    private static final int PORT = 4446; // create the packet

    public static void sendMulticastMessage(String message) {
        try (DatagramSocket socket = new DatagramSocket()) { // creates a UDP socket
            byte[] buf = message.getBytes(); // the message we want to send turned into bytes

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet); // sends the UDP packet to the multicast group

            System.out.println("Multicast message sent to group: " + MULTICAST_ADDRESS + ":" + PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
