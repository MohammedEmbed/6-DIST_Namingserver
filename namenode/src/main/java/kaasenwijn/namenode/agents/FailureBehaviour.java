package kaasenwijn.namenode.agents;


//Import required Java classes
import javax.swing.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//Import required Jade classes
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.Envelope;
import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.NodeSender;
import org.json.JSONObject;
import org.json.JSONTokener;


class FailureBehaviour extends CyclicBehaviour {
    private final static NodeRepository nodeRepository = NodeRepository.getInstance();

    FailureBehaviour(Agent a) {
        super(a);
    }

    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            // ATTENTION!! In order to insert the received message in the queued message list
            // I cannot simply do it e.g.
            // ((DummyAgent)myAgent).getGui().queuedMsgListModel.add(0, (Object) new MsgIndication(msg, MsgIndication.INCOMING, new Date()));
            // WHY?
            // Because this is not thread safe!!!
            // In fact, if this operation is executed from this thread while the AWT Event Dispatching
            // Thread is updating the JList component that shows the queued message list in the DummyAgent
            // GUI (e.g. because the user has just sent a message), this can cause an inconsistency
            // between what is shown in the GUI and what the queued message list actually contains.
            // HOW TO SOLVE THE PROBLEM?
            // I need to request the AWT Event Dispatching Thread to insert the received message
            // in the queued message list!
            // This can be done by using the invokeLater static method of the SwingUtilities class
            // as below

            SwingUtilities.invokeLater(new EDTRequester((FailureAgent)myAgent, msg));
        }
        else {
            block();
        }
    }

    class EDTRequester implements Runnable {
        FailureAgent agent;
        ACLMessage msg;

        EDTRequester(FailureAgent a, ACLMessage m) {
            agent = a;
            msg = m;
        }

        public void run() {
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

            // Move to the next node
            int nextId = NodeRepository.getInstance().getNextId();
            Location nextLocation = here(); //TODO
            System.out.println("[FailureAgent] Done here. Migrating to next node: " + nextId);
            doMove(nextLocation); // This is a stub. We Need JADE mobility container support.

        }
    }

}
