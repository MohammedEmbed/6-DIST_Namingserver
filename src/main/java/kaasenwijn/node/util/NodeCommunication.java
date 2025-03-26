package kaasenwijn.node.util;

import static kaasenwijn.node.util.MulticastSender.sendMulticastMessage;

public class NodeCommunication {

    public void MulticastDiscovery(Integer selfId, String selfIp) {
        sendMulticastMessage(String.valueOf(selfId), selfIp);
    }
}
