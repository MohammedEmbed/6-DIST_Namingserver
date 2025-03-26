package kaasenwijn.node.util;

import static kaasenwijn.node.util.MulticastSender.sendMulticastMessage;

public class NodeCommunication {
    private static final String MULTICAST_ADDRESS = "230.0.0.0"; // multicast group address
    private static final int PORT = 4446; // create the packet

    public void MulticastDiscovery(Integer selfId, String selfIp) {
        sendMulticastMessage(String.valueOf(selfId), selfIp);
    }
}
