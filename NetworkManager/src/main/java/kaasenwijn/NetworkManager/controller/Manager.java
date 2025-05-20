package kaasenwijn.NetworkManager.controller;

import kaasenwijn.NetworkManager.model.Node;
import kaasenwijn.NetworkManager.repository.NodeRepository;
import kaasenwijn.NetworkManager.service.NodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
    @GetMapping("/api/logs")
    @ResponseBody
    public String getCommandOutput() {
        //TODO: retrun logs
        return nodeManager.getLogs("Warre",2011);
    }


    // Start a node
    @GetMapping("/api/node/start")
    @ResponseBody
    public String startNode() {
        //TODO: make dynamic
        return nodeManager.startStopNode(new Node(2012,8011,"Arvo"),false);
    }

    // Stop a node
    @GetMapping("/api/node/stop")
    @ResponseBody
    public String stopNode() {
        //TODO: make dynamic
        return nodeManager.startStopNode(new Node(2012,8011,"Arvo"),true);
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
    public List<Node> getAllNodes() {
        //TODO: make dynamic
        return nodeRepository.getAll();
    }

    // To add a node to the network: it will auto be deployed to the server that is hosting the least amount
    // of nodes already
    @GetMapping("/api/node/add/{name}")
    @ResponseBody
    public void addNode(@PathVariable String name) {
        //TODO: make dynamic
        nodeManager.addNode(name);
    }

}
