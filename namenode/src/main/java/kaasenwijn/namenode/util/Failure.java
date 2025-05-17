package kaasenwijn.namenode.util;

import jade.core.AgentContainer;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.ApiService;
import kaasenwijn.namenode.service.NodeService;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

public class Failure {
    private final static NodeRepository nodeRepository = NodeRepository.getInstance();
    private final static ApiService apiService = new ApiService();
    public static void healthCheck(){
        // https://stackoverflow.com/a/29460716
        try{
            // Send a message to next neighbour
            NodeRepository nodeRepo = NodeRepository.getInstance();
            Neighbor next = nodeRepo.getNext();
            try{
                NodeSender.sendUnicastMessage(next.getIp(),next.getPort(),"health-check");
                System.out.println("[Health-check: previous] Successfully send to: "+ next.getIp() + ":"+ next.getPort());

            }catch (CommunicationException c){
                System.out.println("[Health-check: next] Detected failure!");
                System.out.println("Detected broken next node");
                handleFailure(next.Id);
            }

            Neighbor prev = nodeRepo.getNext();
            try{
                // Send a message to previous neighbour
                NodeSender.sendUnicastMessage(prev.getIp(),prev.getPort(),"health-check");
                System.out.println("[Health-check: next] Successfully send to: "+ prev.getIp() + ":" + prev.getPort());

            }catch (CommunicationException c){
                System.out.println("[Health-check: previous] Detected failure!");
                handleFailure(prev.Id);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void handleFailure(int hash){
        // Request prev and next node parameters of failed node from NS
        JSONObject nbData = NodeService.getNeighbors(hash);
        JSONObject next = nbData.getJSONObject("next");
        JSONObject prev = nbData.getJSONObject("previous");

        // Update previous node, it's next id
        JSONObject dataForPrevious = new JSONObject();
        dataForPrevious.put("next_id", next.getInt("id"));
        try{
            NodeSender.sendUnicastMessage(prev.getString("ip"), prev.getInt("port"), "update_next_id", dataForPrevious);

        }catch (CommunicationException e){
            System.out.println("Updating neighbour info failed!");
        }

        // Update next node, it's prev id
        JSONObject dataForNext = new JSONObject();
        dataForNext.put("previous_id", prev.getInt("id"));
        try{
            NodeSender.sendUnicastMessage(next.getString("ip"), next.getInt("port"), "update_previous_id", dataForNext);
        }catch(CommunicationException e){
            System.out.println("Updating neighbour info failed!");
        }

        // Send HTTP DELETE request to nameserver to remove this node
        apiService.deleteNodeRequestFromHash(hash);

    }


}
