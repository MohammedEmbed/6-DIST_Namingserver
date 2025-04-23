package kaasenwijn.namingserver.repository;

import kaasenwijn.namingserver.model.NodeStructure;

public class NodeRepository {

    private static NodeRepository instance = null;
    private final NodeStructure nodeStructure;

    private NodeRepository(){
        nodeStructure = new NodeStructure();

    }

    public static synchronized NodeRepository getInstance(){
        if(instance == null){
            instance = new NodeRepository();

        }

        return instance;
    }

    public int getCurrentId() {
        return nodeStructure.currentId;
    }

    public void setCurrentId(int currentId) {
        this.nodeStructure.currentId = currentId;
    }

    public String getSelfIp() {
        return nodeStructure.selfIp;
    }

    public void setSelfIp(String selfIp) {
        this.nodeStructure.selfIp = selfIp;
    }

    public String getName() {
        return nodeStructure.name;
    }

    public void setName(String name) {
        this.nodeStructure.name = name;
    }

    public int getSelfPort() {
        return nodeStructure.selfPort;
    }

    public void setSelfPort(int selfPort) {
        this.nodeStructure.selfPort = selfPort;
    }


}
