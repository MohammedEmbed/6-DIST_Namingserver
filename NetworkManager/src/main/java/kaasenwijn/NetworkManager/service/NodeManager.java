package kaasenwijn.NetworkManager.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class NodeManager {
    public String getLogs(String name, int hostPort) {
        StringBuilder output = new StringBuilder();
        try {
            // Assume goscript binary is in the project root
            // ProcessBuilder pb = new ProcessBuilder("go run ./logviewer.go --host  6dist.idlab.uantwerpen.be --port 2011 --name Warre");
            ProcessBuilder pb = new ProcessBuilder(
                    "go", "run", "./logviewer.go",
                    "--port", String.valueOf(hostPort),
                    "--name", name,
                    "--static"
            );
            String cwd = System.getProperty("user.dir");

            // Resolve parent directory "../infrastructure"
            File goDir = new File(cwd).getParentFile(); // go one level up
            File infraDir = new File(goDir, "infrastructure");
            System.out.println(infraDir.getAbsolutePath());
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

    public String startStopNode(String name, int hostPort, int port, boolean kill) {
        StringBuilder output = new StringBuilder();
        try {
            // Assume goscript binary is in the project root
            // ProcessBuilder pb = new ProcessBuilder("go run ./logviewer.go --host  6dist.idlab.uantwerpen.be --port 2011 --name Warre");
            ProcessBuilder pb = new ProcessBuilder(
                    "go", "run", "./manageNode.go",
                    "--host-port", String.valueOf(hostPort),
                    "--port", String.valueOf(port),
                    "--name", name,
                   kill ? "--kill" :""
            );
            String cwd = System.getProperty("user.dir");

            // Resolve parent directory "../infrastructure"
            File goDir = new File(cwd).getParentFile(); // go one level up
            File infraDir = new File(goDir, "infrastructure");
            System.out.println(infraDir.getAbsolutePath());
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

}
