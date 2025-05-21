package kaasenwijn.NetworkManager.repository;

import kaasenwijn.NetworkManager.model.Node;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NodeRepository {
    // Maps which nodes are deployed on which container
    // Containers are identified by its port number
    private final HashMap<Integer, List<Node>> nodeMap = new HashMap<>(); //[Integer, Ip Address] => node
    private final HashMap<String, Node> nameNodeMap = new HashMap<>(); //[Integer, Ip Address] => node
    private final HashMap<String, Boolean> statusNodeMap = new HashMap<>(); //[Integer, Ip Address] => node
    private Boolean NSStatus = false;
    private static NodeRepository single_instance = null;

    private final int portRangeMin = 8000;
    private final int portRangeMax = 8079;
    private final int minPortDistance = 4;

    public Boolean getNSStatus() {
        return NSStatus;
    }

    public void setNSStatus(Boolean NSStatus) {
        this.NSStatus = NSStatus;
    }

    private NodeRepository() {
        nodeMap.put(2011,new ArrayList<>());
        nodeMap.put(2012,new ArrayList<>());
    }

    public static synchronized NodeRepository getInstance() {
        if (single_instance == null)
            single_instance = new NodeRepository();

        return single_instance;
    }

    public List<Node> getNodes(int port) {
        return this.nodeMap.getOrDefault(port,new ArrayList<>());
    }

    public void startNode(Node node){
        this.statusNodeMap.put(node.getName(),true);
    }

    public void stopNode(Node node){
        this.statusNodeMap.put(node.getName(),false);
    }
    public void addNode(Node node){
        List<Node> nodes = getNodes(node.getHostPort());
        nodes.add(node);
        this.nodeMap.put(node.getHostPort(),nodes);
        this.nameNodeMap.put(node.getName(),node);
    }


    public void removeNode(Node node){
        List<Node> nodes = getNodes(node.getHostPort());
        nodes.removeIf(_node -> node.getName().equals(_node.getName()));
        this.nodeMap.put(node.getHostPort(),nodes);
        this.nameNodeMap.remove(node.getName());
        this.statusNodeMap.remove(node.getName());
    }

    public int findLeastLoadedServer(){
        int smallest = Integer.MAX_VALUE;
        int targetPort = 0;
        for(int serverPort: nodeMap.keySet()){
            int amountOfNodes= getNodes(serverPort).size();
            if(nodeMap.get(serverPort).size() < smallest){
                smallest = amountOfNodes;
                targetPort =serverPort;
            }
        }
        return targetPort;
    }
    public int findFreePort(int port) {
        List<Node> nodes = this.getNodes(port);
        for (int candidate = portRangeMin; candidate <= portRangeMax; candidate++) {
            boolean isFree = true;

            for (Node node : nodes) {
                int usedPort = node.getPort();

                // Reserve range: [usedPort - (minPortDistance - 1), usedPort + (minPortDistance - 1)]
                int rangeMin = usedPort - (minPortDistance - 1);
                int rangeMax = usedPort + (minPortDistance - 1);

                if (candidate >= rangeMin && candidate <= rangeMax) {
                    isFree = false;
                    break;
                }
            }

            if (isFree) {
                return candidate;
            }
        }
        return -1;
    }

    public List<Node> getAll(){
        return nodeMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public  Node getNodeByName(String name){
        return this.nameNodeMap.get(name);
    }
    public  Boolean getStatusByName(String name){
        return this.statusNodeMap.getOrDefault(name,false);
    }

}
