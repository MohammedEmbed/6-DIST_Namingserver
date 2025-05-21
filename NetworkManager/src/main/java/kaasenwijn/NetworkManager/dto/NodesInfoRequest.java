package kaasenwijn.NetworkManager.dto;

import kaasenwijn.NetworkManager.model.NodeInfo;

import java.util.List;

public class NodesInfoRequest {
    private List<NodeInfo> info;

    public NodesInfoRequest(List<NodeInfo> info) {
        this.info = info;
    }

    public List<NodeInfo> getInfo() {
        return info;
    }

    public void setInfo(List<NodeInfo> info) {
        this.info = info;
    }


}
