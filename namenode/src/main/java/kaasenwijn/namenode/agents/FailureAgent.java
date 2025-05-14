package kaasenwijn.namenode.agents;

import jade.core.Agent;
import jade.core.Location;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.NodeSender;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Objects;

public class FailureAgent extends Agent {
    private int failedNodeId;
    private int newOwnerId;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            failedNodeId = (int) args[0];
            newOwnerId = (int) args[1];
        } else {
            System.err.println("FailureAgent requires 2 arguments: failedNodeId and newOwnerId.");
            doDelete();
            return;
        }

        System.out.println("[FailureAgent] Started at node: " + NodeRepository.getInstance().getCurrentId());

        String logFolderPath = "logs_" + NodeRepository.getInstance().getName(); //TODO: Is this right?
        File logFolder = new File(logFolderPath);

        if (logFolder.exists()) {
            for (File logFile : Objects.requireNonNull(logFolder.listFiles())) {
                if (!logFile.getName().endsWith(".json")) continue;

                try (FileReader reader = new FileReader(logFile)) {
                    JSONObject log = new JSONObject(new JSONTokener(reader));
                    JSONObject originalOwner = log.getJSONObject("original_owner");

                    if (originalOwner.getInt("node_id") == failedNodeId) {
                        String filename = log.getString("filename");
                        int fileHash = log.getInt("hash");

                        System.out.printf("[FailureAgent] File '%s' (hash %d) is owned by failed node. Reassigning...%n", filename, fileHash);
                        //System.out.println("[FailureAgent] File " + filename + " (hash " + fileHash + ") is owned by failed node. Reassigning...\n");

                        // Update log
                        originalOwner.put("node_id", newOwnerId);

                        try (FileWriter writer = new FileWriter(logFile)) {
                            writer.write(log.toString(2));
                        }

                        // Send file to new owner
                        String newOwnerIp = "ip-here"; //TODO: Lookup via NodeRepository or use IP mapping
                        int newOwnerPort = 9090;   //TODO: should be discovered
                        NodeSender.sendFile(newOwnerIp, newOwnerPort, filename);
                        System.out.printf("[FailureAgent] Sent file '%s' to new owner %s%n", filename, newOwnerIp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Move to the next node
        int nextId = NodeRepository.getInstance().getNextId();
        Location nextLocation = here(); //TODO
        System.out.println("[FailureAgent] Done here. Migrating to next node: " + nextId);
        doMove(nextLocation); // This is a stub. We Need JADE mobility container support.
    }
}
