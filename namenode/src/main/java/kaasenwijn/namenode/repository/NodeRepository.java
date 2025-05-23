package kaasenwijn.namenode.repository;

import jade.wrapper.AgentContainer;
import kaasenwijn.namenode.model.NodeStructure;
import kaasenwijn.namenode.model.Neighbor;

public class NodeRepository {

    private static NodeRepository instance = null;
    private NodeStructure nodeStructure;

    private NodeRepository() {
        nodeStructure = new NodeStructure();
        nodeStructure.namingServerIp = System.getProperty("NS_IP");
        nodeStructure.namingServerPort = Integer.parseInt(System.getProperty("NS_PORT"));

    }

    public static synchronized NodeRepository getInstance() {
        if (instance == null) {
            instance = new NodeRepository();
        }

        return instance;
    }

    public NodeStructure getNodeStructure(){
        return nodeStructure;
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

    public Neighbor getPrevious() {
        return nodeStructure.previous;
    }

    public int getPreviousId() {
        return nodeStructure.previous.Id;
    }


    public void setPrevious(int previousId) {
        nodeStructure.previous = new Neighbor(previousId);
    }

    public Neighbor getNext() {
        return nodeStructure.next;
    }

    public int getNextId() {
        return nodeStructure.next.Id;
    }

    public void setNext(int nextId) {
        nodeStructure.next = new Neighbor(nextId);
    }

    public String getNamingServerIp() {
        return nodeStructure.namingServerIp;
    }

    public int getNamingServerPort(){
        return nodeStructure.namingServerPort;
    }
    public int getNamingServerHTTPPort(){
        return nodeStructure.namingServerHTTPPort;
    }
    public void setNamingServerHTTPPort(int port){
        nodeStructure.namingServerHTTPPort = port;
    }

    public void setAgentContainer(AgentContainer container){
        nodeStructure.agentContainer = container;
    }
    public AgentContainer getAgentContainer(){
        return nodeStructure.agentContainer;
    }

}
