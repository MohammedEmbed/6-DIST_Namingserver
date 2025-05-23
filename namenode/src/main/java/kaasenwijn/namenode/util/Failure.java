package kaasenwijn.namenode.util;

import jade.wrapper.AgentController;
import kaasenwijn.namenode.agents.FailureAgent;
import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.ApiService;
import kaasenwijn.namenode.service.NodeService;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

public class Failure {
    private static final NodeRepository nodeRepository = NodeRepository.getInstance();
    private static final ApiService apiService = new ApiService();

    public static void healthCheck() {
        try {
            Neighbor next = nodeRepository.getNext();
            Neighbor prev = nodeRepository.getPrevious();

            try {
                NodeSender.sendUnicastMessage(next.getIp(), next.getPort(), "health-check");
                System.out.printf("[Health-check: next] Successfully sent to: %s:%d%n", next.getIp(), next.getPort());
            } catch (CommunicationException c) {
                System.out.println("[Health-check: next] Detected failure.");
                handleFailure(next.Id);
            }

            try {
                NodeSender.sendUnicastMessage(prev.getIp(), prev.getPort(), "health-check");
                System.out.printf("[Health-check: previous] Successfully sent to: %s:%d%n", prev.getIp(), prev.getPort());
            } catch (CommunicationException c) {
                System.out.println("[Health-check: previous] Detected failure.");
                handleFailure(prev.Id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleFailure(int hash) {
        JSONObject nbData = NodeService.getNeighbors(hash);
        JSONObject next = nbData.getJSONObject("next");
        JSONObject prev = nbData.getJSONObject("previous");

        JSONObject dataForPrevious = new JSONObject();
        dataForPrevious.put("next_id", next.getInt("id"));
        try {
            NodeSender.sendUnicastMessage(prev.getString("ip"), prev.getInt("port"), "update_next_id", dataForPrevious);
        } catch (CommunicationException e) {
            System.out.println("Updating neighbour info failed.");
        }

        JSONObject dataForNext = new JSONObject();
        dataForNext.put("previous_id", prev.getInt("id"));
        try {
            NodeSender.sendUnicastMessage(next.getString("ip"), next.getInt("port"), "update_previous_id", dataForNext);
        } catch (CommunicationException e) {
            System.out.println("Updating neighbour info failed.");
        }

        apiService.deleteNodeRequestFromHash(hash);
        releaseLocksForFailedNode(hash);
        initiateFailureAgent(hash);
    }

    private static void releaseLocksForFailedNode(int nodeId) {
        String namingServerIp = nodeRepository.getNamingServerIp();
        try {
            URL url = new URL("http://" + namingServerIp + ":" +
                    nodeRepository.getNamingServerHTTPPort() + "/api/lock/release_by_node/" + nodeId);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.printf("Released all locks for sunken node %d from Naming Server.%n", nodeId);
            } else {
                System.err.printf("Failed to release locks for node %d — server responded %d%n", nodeId, responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            System.err.printf("Lock cleanup failed for ghost node %d: %s%n", nodeId, e.getMessage());
        }
    }

    public static void initiateFailureAgent(int hash) {
        System.out.println("[Failure] Initiating FailureAgent!");

        int newOwnerId = nodeRepository.getCurrentId();

        try {
            AgentController ac = nodeRepository.getAgentContainer().createNewAgent(
                    "FailureAgent"+nodeRepository.getName(),
                    FailureAgent.class.getName(),
                    new Object[]{hash, newOwnerId}
            );

            ac.start();
            System.out.printf("[Failure] Started FailureAgent for failed node %d, new owner %d.%n", hash, newOwnerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
