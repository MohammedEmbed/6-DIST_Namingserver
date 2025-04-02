package kaasenwijn.node.service;

import kaasenwijn.namingserver.service.NameService;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class UnicastReceiver extends Thread{
    private final int currentID;
    private int previousID;
    private int nextID;
    NameService nameService;

    public UnicastReceiver(String nodeName) {
        this.currentID = nameService.getHash(nodeName);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8081);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            }
        } catch (IOException e) {
            System.err.println("Error starting UnicastReceiver: " + e.getMessage());
        }
    }
}
