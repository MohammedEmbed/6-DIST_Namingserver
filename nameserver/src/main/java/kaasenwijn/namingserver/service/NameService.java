package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.model.Neighbours;
import kaasenwijn.namingserver.model.NodeIp;
import kaasenwijn.namingserver.repository.IpRepository;
import kaasenwijn.namingserver.repository.NodeRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class NameService {

    private final static IpRepository ipRepo = IpRepository.getInstance();

    /**
     * Implementation of the hashing function as described in the project explanation.
     * It has a bound between 0 and 32 768.
     *
     * @param name the string value to hash
     * @return hash of the name as an integer value between 0 and 32 768
     */
    public static Integer getHash(String name){
        double fac = (double) 32768 /((long) 2*Integer.MAX_VALUE);
        long med = (name.hashCode()  + (long) Integer.MAX_VALUE);
        double result = med * fac;
        return (int) Math.floor(result);
    }

    public static Integer getFileOwnerId(Integer fileHash, int senderId){
        int best = senderId;
        int largest = senderId;
        for(Integer nodeId : IpRepository.getAllIds()){
            if(nodeId < fileHash && nodeId != senderId){
                best =nodeId;
            }
            if(nodeId > largest){
                largest = nodeId;
            }
        }
        return best==senderId ? largest : best;
    }

    public static void startUp(String ip,int port,String name){
        Integer id = getHash(name);
        NodeRepository repo = NodeRepository.getInstance();
        repo.setCurrentId(id);
        repo.setSelfIp(ip);
        repo.setSelfPort(port);
        repo.setName(name);
    }

    public static Neighbours getNeighbours(int id){
        int nextId = ipRepo.getNextId(id);
        int prevId = ipRepo.getPreviousId(id);
        String nextIp = ipRepo.getIp(nextId);
        String prevIp = ipRepo.getIp(prevId);
        return new Neighbours(new NodeIp(nextId,nextIp), new NodeIp(prevId,prevIp));
    }


    public static  JSONObject sendServerGetRequest(String dest, String path){

        try {
            System.out.println("server GET request too: "+"http://" + dest  + path);
            URL url = new URL("http://" + dest + path);
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
                System.out.println("Error: failed GET request with response code: " + responseCode);
                conn.disconnect();
            }


        } catch (Exception e) {
            System.err.println("Error: failed GET request to " + dest);
            e.printStackTrace();
        }
        return null;
    }

    public static  Boolean sendServerStatusCheck(String dest, String path){

        try {
            System.out.println("server GET request too: "+"http://" + dest  + path);
            URL url = new URL("http://" + dest + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;

            } else {
                System.out.println("Error: failed GET request with response code: " + responseCode);
                conn.disconnect();
                return false;
            }


        } catch (Exception e) {
            System.err.println("Error: failed GET request to " + dest);
            e.printStackTrace();
        }
        return false;
    }

}
