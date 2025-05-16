package kaasenwijn.namenode.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.FileMonitor;
import kaasenwijn.namenode.util.NodeSender;
import kaasenwijn.namenode.model.Neighbor;
import org.json.JSONObject;

import java.util.HashMap;

public class SyncAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " is starting for node: "+ System.getenv("SERVER_NAME")+", address: "+System.getenv("SERVER_IP")+":"+System.getenv("SERVER_PORT"));

        HashMap<Integer, String> agentKnownFiles = FileMonitor.getKnownFiles();

        System.out.println("Files owned by this node:");
        for (String filename : agentKnownFiles.values()) {
            System.out.println(" -> " + filename);
        }

        // Start periodic behavior: send file list to next node every 15 seconds
        addBehaviour(new TickerBehaviour(this, 15000) {
            @Override
            protected void onTick() {
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
}
