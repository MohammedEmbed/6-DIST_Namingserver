package kaasenwijn.namenode.agents;

import java.io.*;
import java.util.*;

import jade.core.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.*;
import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.NodeSender;
import org.json.JSONObject;
import org.json.JSONTokener;

// Based on the DummyAgent
class FailureBehaviour extends OneShotBehaviour implements Serializable {
    private final static NodeRepository nodeRepository = NodeRepository.getInstance();

    FailureBehaviour(Agent a) {
        super(a);
    }

    public void action() {
        ACLMessage msg = myAgent.receive();
        FailureAgent agent = (FailureAgent) myAgent;
        if (msg != null) {
            String logFolderPath = "logs_" + nodeRepository.getName(); //TODO: Is this right?
            File logFolder = new File(logFolderPath);

            if (logFolder.exists()) {
                for (File logFile : Objects.requireNonNull(logFolder.listFiles())) {
                    if (!logFile.getName().endsWith(".json")) continue;

                    try (FileReader reader = new FileReader(logFile)) {
                        JSONObject log = new JSONObject(new JSONTokener(reader));
                        JSONObject originalOwner = log.getJSONObject("original_owner");

                        if (originalOwner.getInt("node_id") == agent.failedNodeId) {
                            String filename = log.getString("filename");
                            int fileHash = log.getInt("hash");

                            System.out.printf("[FailureAgent] File '%s' (hash %d) is owned by failed node. Reassigning...%n", filename, fileHash);
                            //System.out.println("[FailureAgent] File " + filename + " (hash " + fileHash + ") is owned by failed node. Reassigning...\n");

                            originalOwner.put("node_id", agent.newOwnerId); // Update log

                            try (FileWriter writer = new FileWriter(logFile)) {
                                writer.write(log.toString(2));
                            }

                            Neighbor newOwner = new Neighbor(agent.newOwnerId); // Lookup IP/port of new owner
                            String newOwnerIp = newOwner.getIp();
                            int newOwnerPort = newOwner.getPort();

                            NodeSender.sendFile(newOwnerIp, newOwnerPort, filename);
                            System.out.printf("[FailureAgent] Sent file '%s' to new owner %s%n", filename, newOwnerIp);
                        }
                    } catch (IOException e) {
                        System.err.println("[FailureAgent] Failed to send file to new owner.");
                        e.printStackTrace();
                    }
                }
            }

            agent.migrateToNextNode();
        } else {
            block();
        }
    }
}
