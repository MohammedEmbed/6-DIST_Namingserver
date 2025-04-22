package kaasenwijn.namenode.service;

import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.CommunicationException;
import kaasenwijn.namenode.util.NodeSender;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import static kaasenwijn.namenode.util.Failure.handleFailure;

@Service
public class NodeService {

    private final static NodeRepository nodeRepository = NodeRepository.getInstance();
    public static void startUp(String ip,int port,String name){
        Integer id = getHash(name);
        NodeRepository repo = NodeRepository.getInstance();
        repo.setCurrentId(id);
        repo.setSelfIp(ip);
        repo.setSelfPort(port);
        repo.setName(name);
        repo.setNext(id);
        repo.setPrevious(id);

        // TODO: fix for lab5, currently makes the server crash
        // verifyLocalFiles();
        // reportLocalFilesToNamingServer();
    }
    //The same hash function as the Namingserver:
    public static Integer getHash(String name){
        double fac = (double) 32768 /((long) 2*Integer.MAX_VALUE);
        long med = (name.hashCode()  + (long) Integer.MAX_VALUE);
        double result = med * fac;
        return (int) Math.floor(result);
    }

    public static JSONObject updateNeighborsData(int hash){
        int currentId = nodeRepository.getCurrentId();

        int previousId = nodeRepository.getPreviousId();
        int nextId = nodeRepository.getNextId();

        JSONObject data = new JSONObject();

        // When network exists of one node and another joins
        if(currentId == nextId && currentId == previousId){
            nodeRepository.setNext(hash);
            nodeRepository.setPrevious(hash);
            // Data to send in unicast to node to say that it's between this node and the nextid of this node
            data.put("previous_id",nodeRepository.getCurrentId());
            data.put("next_id",nodeRepository.getCurrentId());
        }

        if(currentId < hash && hash < nextId){
            nodeRepository.setNext(hash);
            // Data to send in unicast to node to say that it's between this node and the nextid of this node
            data.put("previous_id",nodeRepository.getCurrentId());
            data.put("next_id",hash);


        }else if(previousId < hash && hash < currentId){
            nodeRepository.setPrevious(hash);
            // Data to send in unicast to node to say that it's between the previous node and this node
            data.put("previous_id",hash);
            data.put("next_id",nodeRepository.getCurrentId());
        }
        return data;
    }

    public static void verifyLocalFiles(){
        /*
        De files voor elke node moeten momenteel bijgehouden worden in het mapje files.
        de folder (lijn hier onder) kijkt op die map en lijst de files op die erin zetten.

        Dan extra is nog kort alle local files printen (ter info mag eigenlijk weg)
         */
        File folder = new File("files");
        File[] files = folder.listFiles();

        //Print out local files
        System.out.println("Local files:");
        for (File file : files) {
            if (file.isFile()) {
                System.out.println("- " + file.getName());
            }
        }
    }

    public static void reportLocalFilesToNamingServer() {
        File folder = new File(System.getProperty("user.dir") + File.separator + "files");
        if (!folder.exists()) return;

        NodeRepository repo = NodeRepository.getInstance(); //Instantie opvragen
        String selfIp = repo.getSelfIp(); //Vraag Ip op

        for (File file : folder.listFiles()) { //Lijst alle files van de node op
            if (!file.isFile()) continue;

            String filename = file.getName();
            int fileHash = getHash(filename); //Bereken hun hashes


        }
    }

    public static boolean shouldDrop(JSONObject packet){
        JSONObject source = packet.getJSONObject("source");
        return Objects.equals(nodeRepository.getSelfIp(), source.getString("ip"));

    }


    /** SHUTDOWN state
     * Send the ID of the next node to the previous node. In the previous node,
     *   the next node parameter will be updated according to this information.
     * Send the ID of the previous node to the next node. In the next node, the
     *   previous node parameter will be updated according to this information
     * Remove the node from the Naming server’s Map

     */
    public static void shutdown() {
System.out.println("Shutting down");
        String currentName = nodeRepository.getName();

        Neighbor previous = nodeRepository.getPrevious();
        Neighbor next = nodeRepository.getNext();

        JSONObject dataForPrevious = new JSONObject();
        // Data to send in unicast to previous node to give its new next node
        dataForPrevious.put("next_id", next.Id);
        System.out.println("Info - Current Name: " + currentName + ", Previous ID: " + previous.Id + ", Next ID: " + next.Id+" Data for prev: "+dataForPrevious.toString());
        try{
            NodeSender.sendUnicastMessage(previous.getIp(), previous.getPort(), "update_next_id", dataForPrevious);

        }catch (CommunicationException e){
            // TODO: handle failure
            handleFailure(previous.Id);

        }

        JSONObject dataForNext = new JSONObject();
        // Data to send in unicast to next node to give its new previous node
        dataForNext.put("previous_id", previous.Id);
        System.out.println(dataForNext.toString());
        try{
            NodeSender.sendUnicastMessage(next.getIp(), next.getPort(), "update_previous_id", dataForNext);
        }catch(CommunicationException e){
            // TODO: handle failure
            handleFailure(next.Id);
        }

        // Send HTTP DELETE request to nameserver to remove this node
        String namingServerIp = nodeRepository.getNamingServerIp();
        try {
            URL url = new URL("http://" + namingServerIp + ":8080/api/node/" + currentName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("DELETE request for '" + currentName + "' successfully sent to " + namingServerIp);
            } else {
                System.err.println("Failed to send DELETE request to " + namingServerIp + " — HTTP " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Error DELETE request to " + namingServerIp);
            e.printStackTrace();
        }

    }

    public static JSONObject getNeighbours(int id) throws RuntimeException{

        // Send HTTP DELETE request to nameserver to remove this node
        String namingServerIp = nodeRepository.getNamingServerIp();
        try {

            URL url = new URL("http://" + namingServerIp + ":8080/api/node/nb/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                conn.disconnect();

                // Parse JSON
                String jsonString = response.toString();
                return new JSONObject(jsonString);

            } else {
                System.out.println("GET request failed with response code: " + responseCode);
                conn.disconnect();
                throw new RuntimeException("HTTP communication with nameserver failed");
            }


        } catch (Exception e) {
            System.err.println("Error DELETE request to " + namingServerIp);
            e.printStackTrace();

            throw  new RuntimeException();
        }


    }

}
