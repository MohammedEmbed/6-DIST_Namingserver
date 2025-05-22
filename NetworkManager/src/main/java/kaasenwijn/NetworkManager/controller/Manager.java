package kaasenwijn.NetworkManager.controller;

import kaasenwijn.NetworkManager.model.Node;
import kaasenwijn.NetworkManager.model.NodeInfo;
import kaasenwijn.NetworkManager.repository.NodeRepository;
import kaasenwijn.NetworkManager.service.NodeManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String index(Model model) {
        List<NodeInfo> nodeInfoList = nodeManager.getNodes();
        model.addAttribute("nodes", nodeInfoList);
        model.addAttribute("NSStatus", nodeRepository.getNSStatus());
        System.out.println(nodeRepository.getNSStatus());
        System.out.println(nodeInfoList);
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
    public ResponseEntity<Void> startNode(@PathVariable String name) {
        Node node = nodeRepository.getNodeByName(name);
        nodeRepository.startNode(node);
        nodeManager.startStopNode(node,false);
        return ResponseEntity.ok().build();
    }

    // Stop a node
    @GetMapping("/api/node/stop/{name}")
    @ResponseBody
    public ResponseEntity<Void> stopNode(@PathVariable String name) {
        Node node = nodeRepository.getNodeByName(name);
        nodeRepository.stopNode(node);
        nodeManager.startStopNode(node,true);
        return ResponseEntity.ok().build();
    }


    // Start a the name server
    @GetMapping("/api/ns/start")
    @ResponseBody
    public ResponseEntity<Void> startNS() {
        //TODO: make dynamic
        nodeRepository.setNSStatus(true);
        String output = nodeManager.startStopNS(false);
        boolean status = nodeManager.isNSUp();
        return ResponseEntity.ok().build();
    }

    // Stop the name server
    @GetMapping("/api/ns/stop")
    @ResponseBody
    public ResponseEntity<Void> stopNS() {
        //TODO: make dynamic
        nodeRepository.setNSStatus(false);
        nodeManager.startStopNS(true);
        return ResponseEntity.ok().build();

    }

    // Get all deployed nodes
    @GetMapping("/api/nodes")
    @ResponseBody
    public List<NodeInfo>  getAllNodes() {
        // TO DO: map information from nameserver and this toagether
        return nodeManager.getNodes();

    }

    @GetMapping("/api/node/{name}")
    @ResponseBody
    public NodeInfo  getNode(@PathVariable String name) {
        // TO DO: map information from nameserver and this toagether
        JSONObject nodesJson = nodeManager.sendServerGetRequestObject("localhost:8091","/api/node/info/all");

        NodeInfo nodeInfo = NodeInfo.fromJSONObject(nodesJson);
        Node portInfo = nodeRepository.getNodeByName(name);
        if(portInfo != null){
            nodeInfo.addPortInfo(portInfo);
            nodeInfo.getInfo().setStatus(nodeRepository.getStatusByName(name));
        }
        return nodeInfo;
    }


    // To add a node to the network: it will auto be deployed to the server that is hosting the least amount
    // of nodes already
    @GetMapping("/api/node/add/{name}")
    @ResponseBody
    public ResponseEntity<Void> addNode(@PathVariable String name) {
        nodeManager.addNode(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/node/remove/{name}")
    @ResponseBody
    public ResponseEntity<Void> removeNode(@PathVariable String name) {
        Node node = nodeRepository.getNodeByName(name);
        if(nodeRepository.getStatusByName(name)){
            nodeManager.startStopNode(node,true);
        }
        nodeRepository.removeNode(node);

        return ResponseEntity.ok().build();
    }

}
