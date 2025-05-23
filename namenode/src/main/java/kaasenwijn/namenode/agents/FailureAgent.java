package kaasenwijn.namenode.agents;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;

import java.io.Serializable;

// Example mobile agent: https://github.com/ekiwi/jade-mirror/tree/master/src/examples/mobile

/**
 * Failure agent is started as soon as a node failure is detected. The responsibility of this agent is to
 * 1. transfer all the files from a failed node to the new owner, and to
 * 2. update the whole file list.
 */
public class FailureAgent extends Agent implements Runnable, Serializable {
/*
    To develop Failure Agent class that implements the Runnable and Serializable interface:
    - The failing node id is added to the constructor,
    - The current node id is added to the constructor (the node that started Failure agent)
    - The run() method will perform:
        1. Reading the file list of the current node
        2. If the failing node is the owner of the file, then the file should be transferred to the new owner, but there are two options:
            1. Option 1: if the new owner doesn’t have a copy of this file already, then the file transfer can be
                done without any problems. The logs should be updated with a new download location.
            2. Option 2: if the file is already stored on the new owner, only the log should be updated
    - Terminate the Failure Agent when it passed all nodes in the ring topology
        - If the node id is equal to the node id that started the agent -> terminate the Agent
 */

    protected int failedNodeId;
    protected int newOwnerId;
    protected int initialNodeId;

    private final static NodeRepository nodeRepository = NodeRepository.getInstance();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            failedNodeId = (int) args[0];
            newOwnerId = (int) args[1];
            initialNodeId = newOwnerId;
            System.out.println("[FailureAgent] Started at node: " + initialNodeId);
        } else {
            System.err.println("[FailureAgent] Not started: requires 2 arguments: failedNodeId and newOwnerId.");
            doDelete();
            return;
        }
        executeAgent();
    }

    protected void executeAgent() {
        // Add agent behaviour
        Behaviour b = new FailureBehaviour(this);
        addBehaviour(b);
    }

    public void migrateToNextNode() {
        Neighbor next = NodeRepository.getInstance().getNext();
        Location nextLocation = getNextLocation(next);
        System.out.println("[FailureAgent] Done here. Migrating to next node: " + next.Id);
        doMove(nextLocation);
    }

    private Location getNextLocation(Neighbor next) {
        // TODO: Do we need an AMS? An agent cannot create locations by itself
        //  https://jade.tilab.com/doc/programmersguide.pdf 3.7.1 JADE API for agent mobility
        //  "application agents are not allowed to create their own locations. Instead, they must
        //  ask the AMS for the list of the available locations and choose one. Alternatively, a JADE agent
        //  can also request the AMS to tell where (at which location) another agent lives."
        // TODO: Check if this is a suitable alternative to an AMS
        return new Location() {
            @Override
            public String getID() {
                return next.Id.toString();
            }

            @Override
            public String getName() {
                return getID(); // TODO: Maybe add a getName to the NS API?
            }

            @Override
            public String getProtocol() {
                return "JADE-IPMT"; // Internal Platform Message Transport
            }

            @Override
            public String getAddress() {
                // Structure: Zadig:1099/JADE.Container-1
                String resp = next.getIp() + ":" + (next.getPort() + 1) + "/JADE.Main-Container";
                return resp;
            }
        };
    }

    @Override
    protected void afterMove() {
        newOwnerId = nodeRepository.getCurrentId();
        System.out.println("[FailureAgent] " + getLocalName() + " is just arrived to this location: " + newOwnerId);
        // Terminate the agent if it has passed all nodes
        if (newOwnerId == initialNodeId) {
            doDelete();
        }
        executeAgent();
    }
}

