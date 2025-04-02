package kaasenwijn.node.repository;

import kaasenwijn.node.model.Neighbor;

public class NodeRepository {

    private static NodeRepository instance = null;
    private int currentId;
    private String selfIp;

    private int previousId;
    private int nextId;

    private NodeRepository(){

    }

    public static synchronized NodeRepository getInstance(){
        if(instance == null){
            instance = new NodeRepository();
        }

        return instance;
    }

    public int getCurrentId() {
        return currentId;
    }

    public void setCurrentId(int currentId) {
        this.currentId = currentId;
    }

    public String getSelfIp() {
        return selfIp;
    }

    public void setSelfIp(String selfIp) {
        this.selfIp = selfIp;
    }

    public int getPreviousId() {
        return previousId;
    }

    public void setPreviousId(int previousId) {
        this.previousId = previousId;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }
}
