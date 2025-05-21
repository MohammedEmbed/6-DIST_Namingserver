package kaasenwijn.NetworkManager.controller;

import kaasenwijn.NetworkManager.model.Node;
import kaasenwijn.NetworkManager.model.NodeInfo;
import kaasenwijn.NetworkManager.repository.NodeRepository;
import kaasenwijn.NetworkManager.service.NodeManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@Controller
public class Manager {

    @Autowired
    private NodeManager nodeManager;

    private final NodeRepository nodeRepository = NodeRepository.getInstance();

    // Returns html index page
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Get logs of a node
    @GetMapping("/api/logs/{name}")
    @ResponseBody
    public String getCommandOutput(@PathVariable String name) {
        //TODO: retrun logs
        Node node = nodeRepository.getNodeByName(name);
        return nodeManager.getLogs(node);
    }


    // Start a node
    @GetMapping("/api/node/start/{name}")
    @ResponseBody
    public String startNode(@PathVariable String name) {
        Node node = nodeRepository.getNodeByName(name);
        nodeRepository.startNode(node);
        return nodeManager.startStopNode(node,false);
    }

    // Stop a node
    @GetMapping("/api/node/stop/{name}")
    @ResponseBody
    public String stopNode(@PathVariable String name) {
        Node node = nodeRepository.getNodeByName(name);
        nodeRepository.stopNode(node);
        return nodeManager.startStopNode(node,true);
    }


    // Start a the name server
    @GetMapping("/api/ns/start")
    @ResponseBody
    public void startNS() {
        //TODO: make dynamic
        nodeManager.startStopNS(false);
    }

    // Stop the name server
    @GetMapping("/api/ns/stop")
    @ResponseBody
    public void stopNS() {
        //TODO: make dynamic
        nodeManager.startStopNS(true);
    }

    // Get all deployed nodes
    @GetMapping("/api/nodes")
    @ResponseBody
    public List<NodeInfo>  getAllNodes() {
        // TO DO: map information from nameserver and this toagether
        JSONArray nodesJson = nodeManager.sendServerGetRequestArray("localhost:8091","/api/node/info/all");

        List<NodeInfo> nodeInfoList = NodeInfo.fromJSONArray(nodesJson);
        for(NodeInfo node: nodeInfoList){
            Node portInfo = nodeRepository.getNodeByName(node.getInfo().getName());
            if(portInfo != null){
                node.addPortInfo(portInfo);
            }
        }
         return nodeInfoList;
    }

    @GetMapping("/api/node/{name}")
    @ResponseBody
    public NodeInfo  getNode(@PathVariable String name) {
        // TO DO: map information from nameserver and this toagether
        JSONObject nodesJson = nodeManager.sendServerGetRequestObject("localhost:8091","/api/node/info/all");

        NodeInfo nodeInfo = NodeInfo.fromJSONObject(nodesJson);
        Node portInfo = nodeRepository.getNodeByName(nodeInfo.getInfo().getName());
        if(portInfo != null){
            nodeInfo.addPortInfo(portInfo);
        }
        return nodeInfo;
    }


    // To add a node to the network: it will auto be deployed to the server that is hosting the least amount
    // of nodes already
    @GetMapping("/api/node/add/{name}")
    @ResponseBody
    public void addNode(@PathVariable String name) {
        nodeManager.addNode(name);
    }

    @GetMapping("/api/node/remove/{name}")
    @ResponseBody
    public void removeNode(@PathVariable String name) {
        Node node = nodeRepository.getNodeByName(name);
        nodeRepository.removeNode(node);
    }

}
