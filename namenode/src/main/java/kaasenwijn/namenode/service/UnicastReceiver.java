package kaasenwijn.namenode.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class UnicastReceiver extends Thread{
//    private final int currentID;
    private int previousID;
    private int nextID;
    NodeService nodeService;

    private int port;
    private String ip;

    public UnicastReceiver(String ip, int port) {
        this.port = port;
        this.ip = ip;
//        this.currentID = nodeService.getHash(nodeName);
    }

    @Override
    public void run() {
        try {
            InetAddress bindAddress = InetAddress.getByName(this.ip);
            ServerSocket serverSocket = new ServerSocket(this.port,50,bindAddress);
            System.out.println("Socked opened on: "+this.ip+":"+this.port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                System.out.println("Unicast message received!");
//              // TO DO: do something with the response
            }
        } catch (IOException e) {
            System.err.println("Error starting UnicastReceiver: " + e.getMessage());
        }
    }
}
