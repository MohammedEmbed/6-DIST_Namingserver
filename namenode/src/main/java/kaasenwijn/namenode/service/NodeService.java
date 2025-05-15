package kaasenwijn.namenode.service;

import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.CommunicationException;
import kaasenwijn.namenode.util.NodeSender;
import kaasenwijn.namenode.util.NodeUnicastReceiver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import static kaasenwijn.namenode.util.Failure.handleFailure;

@Service
public class NodeService {

    private final static NodeRepository nodeRepository = NodeRepository.getInstance();

    public static void startUp(String ip, int port, String name) {
        Integer id = getHash(name);
        NodeRepository repo = NodeRepository.getInstance();
        repo.setCurrentId(id);
        repo.setSelfIp(ip);
        repo.setSelfPort(port);
        repo.setName(name);
        repo.setNext(id);
        repo.setPrevious(id);

    }

    /**
     * The same hash function as the Naming Server
     */
    public static Integer getHash(String name) {
        double fac = (double) 32768 / ((long) 2 * Integer.MAX_VALUE);
        long med = (name.hashCode() + (long) Integer.MAX_VALUE);
        double result = med * fac;
        return (int) Math.floor(result);
    }

    public static JSONObject updateNeighborsData(int hash) {
        int currentId = nodeRepository.getCurrentId();

        int previousId = nodeRepository.getPreviousId();
        int nextId = nodeRepository.getNextId();

        JSONObject data = new JSONObject();

        // When network exists of one node and another joins
        if (currentId == nextId && currentId == previousId) {
            nodeRepository.setNext(hash);
            nodeRepository.setPrevious(hash);
            // Data to send in unicast to node to say that it's between this node and the nextid of this node
            data.put("previous_id", nodeRepository.getCurrentId());
            data.put("next_id", nodeRepository.getCurrentId());
        }

        if (currentId < hash && hash < nextId) {
            nodeRepository.setNext(hash);
            // Data to send in unicast to node to say that it's between this node and the nextid of this node
            data.put("previous_id", nodeRepository.getCurrentId());
            data.put("next_id", hash);
        } else if (previousId < hash && hash < currentId) {
            nodeRepository.setPrevious(hash);
            // Data to send in unicast to node to say that it's between the previous node and this node
            data.put("previous_id", hash);
            data.put("next_id", nodeRepository.getCurrentId());
        }
        return data;
    }

    public static boolean shouldDrop(JSONObject packet) {
        JSONObject source = packet.getJSONObject("source");
        return Objects.equals(nodeRepository.getSelfIp(), source.getString("ip"));

    }


    /**
     * SHUTDOWN state
     * Send the ID of the next node to the previous node. In the previous node,
     * the next node parameter will be updated according to this information.
     * Send the ID of the previous node to the next node. In the next node, the
     * previous node parameter will be updated according to this information
     * Remove the node from the Naming server’s Map
     */
    public static void shutdown() {
        //TODO: 3, Notify owners of this node's local files that the file can be removed (unless downloaded by other nodes?? -> this never happens)
        //Transfer all replicated files to the previous node directly through unicast messages
        Neighbor previousNode = NodeRepository.getInstance().getPrevious();

        String replicationPath = "replicated_files_"+NodeRepository.getInstance().getName();
        File replicationDir = new File(replicationPath);
        if (replicationDir.exists() && replicationDir.isDirectory()) {
            File[] replicationFiles = replicationDir.listFiles();
            if (replicationFiles != null) {

                for (File file : replicationFiles) {//always send all replicated files to previousNode, on receive it will handle edge cases
                    String filename = file.getName();
                    JSONObject data = new JSONObject();
                    int fileHash = NodeService.getHash(filename);
                    String logFileName = "replication_log_" + fileHash + ".json";
                    String logFilePath = "logs_"+nodeRepository.getName() +"/"+logFileName;
                    JSONObject logData = new JSONObject();
                    try{//update the log of the file to remove old node hash
                        logData = readJson(logFilePath);
                        JSONArray downloadArray = logData.getJSONArray("downloaded_locations");
                        for(int i = 0; i < downloadArray.length();i++){
                            if(downloadArray.getJSONObject(i).get("node_id").equals(nodeRepository.getCurrentId())){
                                downloadArray.remove(i);
                            }
                        }
                        logData.put("downloaded_locations",downloadArray);
                    }catch (Exception e){
                        System.out.println("Failed to read log file!");
                        return;
                    }
                    data.put("fileName", filename);
                    data.put("fileHash", fileHash);
                    data.put("logFileName",logFileName);
                    data.put("logFile", logData);

                    try {
                        //Send the file
                        NodeSender.sendUnicastMessage(
                                previousNode.getIp(),
                                previousNode.getPort(),
                                "shutdown_replication",
                                data
                        );

                        NodeUnicastReceiver.deleteFile(logFilePath);
                        NodeUnicastReceiver.deleteFile(replicationPath+"/"+filename);


                        System.out.println("Successfully sent " + filename + " and log to previous node.");
                    } catch (CommunicationException e) {
                        System.err.println("Failed to send " + filename + " and log to previous node.");
                        e.printStackTrace();
                    }
                }
            }
        }

        //Notify owners of local files to remove it if not downloaded.
        String localPath = "replicated_files_"+NodeRepository.getInstance().getName();
        File localDir = new File(localPath);
        if (localDir.exists() && localDir.isDirectory()) {
            File[] localFiles = localDir.listFiles();
            if (localFiles != null) {
                for (File localFile : localFiles){

                }
            }
        }

        //shutdown
        System.out.println("Shutting down");
        String currentName = nodeRepository.getName();

        Neighbor previous = nodeRepository.getPrevious();
        Neighbor next = nodeRepository.getNext();

        JSONObject dataForPrevious = new JSONObject();
        // Data to send in unicast to previous node to give its new next node
        dataForPrevious.put("next_id", next.Id);
        System.out.println("Info - Current Name: " + currentName + ", Previous ID: " + previous.Id + ", Next ID: " + next.Id + " Data for prev: " + dataForPrevious.toString());
        try {
            NodeSender.sendUnicastMessage(previous.getIp(), previous.getPort(), "update_next_id", dataForPrevious);

        }catch (CommunicationException e){
            handleFailure(previous.Id);

        }

        JSONObject dataForNext = new JSONObject();
        // Data to send in unicast to next node to give its new previous node
        dataForNext.put("previous_id", previous.Id);
        System.out.println(dataForNext.toString());
        try {
            NodeSender.sendUnicastMessage(next.getIp(), next.getPort(), "update_previous_id", dataForNext);
        }catch(CommunicationException e){
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

    public static JSONObject getNeighbours(int id) throws RuntimeException {

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
            System.err.println("Error GET request to " + namingServerIp);
            e.printStackTrace();
            throw new RuntimeException();
        }


    }

    // TODO: refactor to API wrapper
    public static JSONObject getFileReplicationLocation(int filehash) throws RuntimeException {
        int currentHash = nodeRepository.getCurrentId();
        // Send HTTP DELETE request to nameserver to remove this node
        String namingServerIp = nodeRepository.getNamingServerIp();
        try {

            URL url = new URL("http://" + namingServerIp + ":8080/api/file/location/" + currentHash+"/"+filehash);
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
            System.err.println("Error GET request to " + namingServerIp);
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    private static  JSONObject readJson(String filePath) throws Exception,IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("File not found");
        }
        try (FileReader reader = new FileReader(file)) {
            // Parse JSON
            return  new JSONObject(new JSONTokener(reader));

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
