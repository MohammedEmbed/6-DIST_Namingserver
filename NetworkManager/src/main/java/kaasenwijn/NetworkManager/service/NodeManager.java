package kaasenwijn.NetworkManager.service;

import kaasenwijn.NetworkManager.model.Node;
import kaasenwijn.NetworkManager.repository.NodeRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class NodeManager {
    private final NodeRepository nodeRepository = NodeRepository.getInstance();

    public String getLogs(String name, int hostPort) {

        // Assume goscript binary is in the project root
        // ProcessBuilder pb = new ProcessBuilder("go run ./logviewer.go --host  6dist.idlab.uantwerpen.be --port 2011 --name Warre");
        ProcessBuilder pb = new ProcessBuilder(
                "go", "run", "./logviewer.go",
                "--port", String.valueOf(hostPort),
                "--name", name,
                "--static"
        );
        String output = runGoScript(pb);

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
       String output = runGoScript(pb);

        return output.toString();
    }

    public String runGoScript(ProcessBuilder pb) {
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
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
        } catch (Exception e) {
            output.append("Error running Go script: ").append(e.getMessage());
        }

        return output.toString();
    }

    public void startStopNS(boolean kill){
        ProcessBuilder pb = new ProcessBuilder(
                "go", "run", "./manageNS.go",
                kill ? "--kill" :""
        );
        runGoScript(pb);
    }

    public void addNode(String name){
        // TO DO: handle case if no nodes are free
        int targetPort = nodeRepository.findLeastLoadedServer();
        int freePort = nodeRepository.findFreePort(targetPort);
        Node node=new Node(targetPort,freePort,name);
        startStopNode(node,false);
        nodeRepository.addNode(node);
        System.out.println("Added "+node);

    }
}
