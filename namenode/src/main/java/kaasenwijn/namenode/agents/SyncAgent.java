package kaasenwijn.namenode.agents;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.FileMonitor;
import kaasenwijn.namenode.util.NodeSender;
import kaasenwijn.namenode.model.Neighbor;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

public class SyncAgent extends Agent implements Runnable, Serializable {
    private static final long SYNC_INTERVAL_MS = 15000;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " is starting for node: "+ System.getProperty("SERVER_NAME")+", address: "+System.getProperty("SERVER_IP")+":"+System.getProperty("SERVER_PORT"));

        /// Register service with DF
        /// "DFAgentDescription is a JADE class used to register an agent's services with the Directory Facilitator (DF)"
        /// Explanation from the tutorial : when agents want to:
        /// - Advertise what they can do
        /// - Find other agents offering a service
        /// They use DFService with a DFAgentDescription
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("sync-agent");
            sd.setName("file-sync"); //Todo: Elke node moet een unieke naam hebben
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            System.err.println("[SyncAgent] DF registration failed.");
        }
        /// Example: How Another Agent Finds It
        ///DFAgentDescription template = new DFAgentDescription();
        /// ServiceDescription sd = new ServiceDescription();
        /// sd.setType("sync-agent");
        /// template.addServices(sd);
        ///
        /// DFAgentDescription[] result = DFService.search(this, template);

        addBehaviour(new TickerBehaviour(this, SYNC_INTERVAL_MS) { // 15 second interval
            @Override
            protected void onTick() {
                HashMap<Integer, String> agentKnownFiles = FileMonitor.getKnownFiles();
                Neighbor next = NodeRepository.getInstance().getNext();
                if (next == null) {
                    System.out.println("[SyncAgent] No next neighbor defined.");
                    return;
                }

                JSONObject fileData = new JSONObject();
                for (Integer hash : agentKnownFiles.keySet()) {
                    fileData.put(hash.toString(), agentKnownFiles.get(hash));
                }

                try {
                    NodeSender.sendUnicastMessage(next.getIp(), next.getPort(), "file_list_sync", fileData);
                    System.out.println("[SyncAgent] Sent file list to neighbor " + next.getIp());
                } catch (Exception e) {
                    System.err.println("[SyncAgent] Failed to send file list to neighbor.");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("[SyncAgent] Shutting down.");
        // Deregister from DF
        try {
            DFService.deregister(this);
        } catch (Exception e) {
            System.err.println("[SyncAgent] DF deregistration failed.");
        }

    }
}

