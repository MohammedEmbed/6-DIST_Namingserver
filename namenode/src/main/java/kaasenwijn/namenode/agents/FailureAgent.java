package kaasenwijn.namenode.agents;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.NodeSender;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Objects;


/**
 * Failure agent is started as soon as a node failure is detected. The responsibility of this agent is to
 * 1. transfer all the files from a failed node to the new owner, and to
 * 2. update the whole file list.
 */
public class FailureAgent extends Agent {
/*
To develop Failure Agent class that implements the Runnable and Serializable interface:
- The failing node id is added to the constructor,
- The current node id is added to the constructor (the node that started Failure agent)
- The run() method will perform:
    1. Reading the file list of the current node
    2. If the failing node is the owner of the file, then the file should be transferred to the new owner, but there are two options:
        1. Option 1: if the new owner doesn’t have a copy of this file already, then the file transfer can be
            done without any problems. The logs should be updated with a new download location.
        2. o Option 2: if the file is already stored on the new owner, only the log should be updated
- Terminate the Failure Agent when it passed all nodes in the ring topology
    - If the node id is equal to the node id that started the agent -> terminate the Agent


 */

    protected int failedNodeId;
    protected int newOwnerId;

    private final static NodeRepository nodeRepository = NodeRepository.getInstance();

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
        // Add agent behaviour
        Behaviour b = new FailureBehaviour(this);
        addBehaviour(b);

        System.out.println("[FailureAgent] Started at node: " + NodeRepository.getInstance().getCurrentId());

    }

}
