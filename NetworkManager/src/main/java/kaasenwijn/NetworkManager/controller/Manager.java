package kaasenwijn.NetworkManager.controller;

import kaasenwijn.NetworkManager.model.Node;
import kaasenwijn.NetworkManager.model.NodeInfo;
import kaasenwijn.NetworkManager.repository.NodeRepository;
import kaasenwijn.NetworkManager.service.NodeManager;
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
        return "index";
    }

    // Return html detail page
    @GetMapping("/detail/{name}")
    public String detail(Model model, @PathVariable String name) {
        NodeInfo node = nodeManager.getNode(name);
        model.addAttribute("node", node);
        model.addAttribute("NSStatus", nodeRepository.getNSStatus());
        return "detail";
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
        nodeManager.isNodeUp(name);
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


    // Start naming server
    @GetMapping("/api/ns/start")
    @ResponseBody
    public ResponseEntity<Void> startNS() {
        //TODO: make dynamic
        nodeRepository.setNSStatus(true);
        String output = nodeManager.startStopNS(false);
        boolean status = nodeManager.isNSUp();
        return ResponseEntity.ok().build();
    }

    // Stop naming server
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
        return nodeManager.getNodes();

    }

    // Get a node
    @GetMapping("/api/node/{name}")
    @ResponseBody
    public NodeInfo getNode(@PathVariable String name) {
        return nodeManager.getNode(name);
    }


    // Add a node, but it isn't deployed yet!
    @GetMapping("/api/node/add/{name}")
    @ResponseBody
    public ResponseEntity<Void> addNode(@PathVariable String name) {
        nodeManager.addNode(name);
        return ResponseEntity.ok().build();
    }

    // Remove a node
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
