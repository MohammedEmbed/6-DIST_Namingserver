package kaasenwijn.namenode.util;

import static kaasenwijn.namenode.util.MulticastSender.sendMulticastMessage;

public class NodeCommunication {

    public void MulticastDiscovery(Integer selfId, String selfIp, int port) {
        sendMulticastMessage(String.valueOf(selfId), selfIp, port);
    }
}
