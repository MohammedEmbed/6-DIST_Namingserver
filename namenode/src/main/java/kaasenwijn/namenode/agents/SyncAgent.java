package kaasenwijn.namenode.agents;

import jade.core.Agent;
import kaasenwijn.namenode.service.FileMonitor;

import java.util.HashMap;

public class SyncAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " is starting...");

        HashMap<Integer, String> knownFiles = FileMonitor.getKnownFiles();

        System.out.println("Files owned by this node:");
        for (String filename : knownFiles.values()) {
            System.out.println(" -> " + filename);
        }

        // Now you're ready to continue with Step 2: sync this list with neighbors
    }
}
