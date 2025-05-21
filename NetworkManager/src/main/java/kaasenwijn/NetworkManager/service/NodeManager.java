package kaasenwijn.NetworkManager.service;

import kaasenwijn.NetworkManager.model.Node;
import kaasenwijn.NetworkManager.repository.NodeRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class NodeManager {
    private final NodeRepository nodeRepository = NodeRepository.getInstance();

    public String getLogs(Node node) {

        // Assume goscript binary is in the project root
        // ProcessBuilder pb = new ProcessBuilder("go run ./logviewer.go --host  6dist.idlab.uantwerpen.be --port 2011 --name Warre");
        ProcessBuilder pb = new ProcessBuilder(
                "go", "run", "./logviewer.go",
                "--port", String.valueOf(node.getHostPort()),
                "--name", node.getName(),
                "--static"
        );
        String output = runGoScript(pb,false);

        return output.toString();
    }

    public String startStopNode(Node node, boolean kill) {

        // Assume goscript binary is in the project root
        // ProcessBuilder pb = new ProcessBuilder("go run ./logviewer.go --host  6dist.idlab.uantwerpen.be --port 2011 --name Warre");
        ProcessBuilder pb = new ProcessBuilder(
                "go", "run", "./manageNode.go",
                "--host-port", String.valueOf(node.getHostPort()),
                "--port", String.valueOf(node.getPort()),
                "--name", node.getName(),
               kill ? "--kill" :""
        );
       String output = runGoScript(pb,false);

        return output.toString();
    }

    public String runGoScript(ProcessBuilder pb, Boolean background) {
        StringBuilder output = new StringBuilder();
        try {
            // Assume goscript binary is in the project root
            // ProcessBuilder pb = new ProcessBuilder("go run ./logviewer.go --host  6dist.idlab.uantwerpen.be --port 2011 --name Warre");
            String cwd = System.getProperty("user.dir");

            // Resolve parent directory "../infrastructure"
            File goDir = new File(cwd).getParentFile(); // go one level up
            File infraDir = new File(goDir, "infrastructure");
            pb.directory(infraDir);

            Process process = pb.start();
            if(!background){
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
                return output.toString();
            }

        } catch (Exception e) {
            output.append("Error running Go script: ").append(e.getMessage());
        }
        return "";

    }

    public String startStopNS(boolean kill){
        ProcessBuilder pb = new ProcessBuilder(
                "go", "run", "./manageNS.go",
                kill ? "--kill" :""
        );
        return runGoScript(pb,false);
    }

    public void startTunnelNS(){
        ProcessBuilder pb = new ProcessBuilder(
                "go", "run", "./tunnel.go"
        );
        runGoScript(pb,true);
    }


    public void addNode(String name){
        // TO DO: handle case if no nodes are free
        int targetPort = nodeRepository.findLeastLoadedServer();
        int freePort = nodeRepository.findFreePort(targetPort);
        Node node=new Node(targetPort,freePort,name);
        nodeRepository.addNode(node);
        //        startStopNode(node,false);
//        nodeRepository.startNode(node);

        System.out.println("Added "+node);

    }

    public   JSONObject sendServerGetRequestObject(String dest, String path){


        String jsonString = sendServerGetRequestGetJsonString(dest,path);
        return new JSONObject(jsonString);
    }

    public  JSONArray sendServerGetRequestArray(String dest, String path){


        String jsonString = sendServerGetRequestGetJsonString(dest,path);
        return new JSONArray(jsonString);
    }

    public   String sendServerGetRequestGetJsonString(String dest, String path){

        try {
            System.out.println("server GET request too: "+"http://" + dest  + path);
            URL url = new URL("http://" + dest + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                conn.disconnect();

                // Parse JSON
                String jsonString = response.toString();
                return jsonString;

            } else {
                System.out.println("Error: failed GET request with response code: " + responseCode);
                conn.disconnect();
            }


        } catch (Exception e) {
            System.err.println("Error: failed GET request to " + dest);
            e.printStackTrace();
        }
        return null;
    }


}
