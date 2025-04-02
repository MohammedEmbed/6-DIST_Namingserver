package kaasenwijn.namenode.repository;

import kaasenwijn.namenode.model.NodeStructure;
import kaasenwijn.namenode.model.Neighbor;

public class NodeRepository {

    private static NodeRepository instance = null;
    private NodeStructure nodeStructure;

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

    public int getPreviousId() {
        return nodeStructure.previousId.Id;
    }

    public void setPreviousId(int previousId) {
        nodeStructure.previousId = new Neighbor(previousId, "127.0.0.1");
    }

    public int getNextId() {
        return nodeStructure.nextId.Id;
    }

    public void setNextId(int nextId) {
        nodeStructure.nextId = new Neighbor(nextId, "127.0.0.1");
    }
}
