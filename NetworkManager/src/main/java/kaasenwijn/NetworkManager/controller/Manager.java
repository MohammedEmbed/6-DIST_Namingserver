package kaasenwijn.NetworkManager.controller;

import kaasenwijn.NetworkManager.service.NodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Controller
public class Manager {

    @Autowired
    private NodeManager nodeManager;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/api/logs")
    @ResponseBody
    public String getCommandOutput() {
        //TODO: retrun logs
        return nodeManager.getLogs("Warre",2011);
    }

    @GetMapping("/api/start")
    @ResponseBody
    public String startNode() {
        //TODO: make dynamic
        return nodeManager.startStopNode("Arvo",2012,8011,false);
    }

    @GetMapping("/api/stop")
    @ResponseBody
    public String stopNode() {
        //TODO: make dynamic
        return nodeManager.startStopNode("Arvo",2012,8011,true);
    }

}
