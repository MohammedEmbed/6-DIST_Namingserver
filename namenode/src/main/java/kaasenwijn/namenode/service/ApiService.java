package kaasenwijn.namenode.service;

import kaasenwijn.namenode.repository.NodeRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ApiService {
    private final static NodeRepository nodeRepository = NodeRepository.getInstance();

    private int sendServerDeleteRequest(String namingServerIp, String path, String type){

        try {
            URL url = new URL("http://" + namingServerIp + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            switch (type) {
                case "DELETE", "GET", "POST" -> {
                    conn.setDoOutput(true);
                    conn.setRequestMethod(type);
                    conn.setRequestProperty("Content-Type", "application/json");
                }

                default-> throw new IllegalStateException("Unexpected type: " + type);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode;

        } catch (Exception e) {

            System.err.println("Error " + type + " request to " + namingServerIp);
            e.printStackTrace();
            return 500;
        }
    }
    private JSONObject sendServerGetRequest(String namingServerIp, String path, String type){

        try {
            URL url = new URL("http://" + namingServerIp + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(type);
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
                System.out.println("Error: "+type+" request failed with response code: " + responseCode);
                conn.disconnect();
            }


        } catch (Exception e) {
            System.err.println("Error: "+type+ " request to " + namingServerIp);
            e.printStackTrace();
        }
        return null;
    }

    public void deleteNodeReqeust(String currentName) {
        String namingServerIp = nodeRepository.getNamingServerIp();
        String path = ":8080/api/node/" + currentName;
        int responseCode = sendServerDeleteRequest(namingServerIp, path, "DELETE");
        if (responseCode == 200) {
            System.out.println("DELETE request for '" + currentName + "' successfully sent to " + namingServerIp);

        } else {
            System.err.println("Failed to send DELETE request to " + namingServerIp + " — HTTP " + responseCode);

        }
    }

    public JSONObject getNeighborsRequest(int currentId){
        String namingServerIp = nodeRepository.getNamingServerIp();
        String path = ":8080/api/node/nb/" + currentId;
        return sendServerGetRequest(namingServerIp,path,"GET");
    }

}
