package kaasenwijn.NetworkManager.dto;

import kaasenwijn.NetworkManager.model.NodeInfo;

public class NodeInfoRequest {
    private NodeInfo info;

    public NodeInfoRequest(NodeInfo info) {
        this.info = info;
    }

    public NodeInfo getInfo() {
        return info;
    }

    public void setInfo(NodeInfo info) {
        this.info = info;
    }
}
