package kaasenwijn.namenode.service;

import kaasenwijn.namenode.repository.NodeRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ApiService {
    private final static NodeRepository nodeRepository = NodeRepository.getInstance();

    private int sendServerDeleteRequest(String namingServerIp, String path){

        try {
            URL url = new URL("http://" + namingServerIp +":"+nodeRepository.getNamingServerHTTPPort() + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");



            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode;

        } catch (Exception e) {

            System.err.println("Error: failed DELETE request to " + namingServerIp);
            e.printStackTrace();
            return 500;
        }
    }
    public JSONObject sendServerGetRequest(String namingServerIp, String path){

        try {
            System.out.println("server GET request too: "+"http://" + namingServerIp +":"+nodeRepository.getNamingServerHTTPPort() + path);
            URL url = new URL("http://" + namingServerIp +":"+nodeRepository.getNamingServerHTTPPort() + path);
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
            System.err.println("Error: failed GET request to " + namingServerIp);
            e.printStackTrace();
        }
        return null;
    }

    public void deleteNodeRequest(String currentName) {
        String namingServerIp = nodeRepository.getNamingServerIp();
        String path = "/api/node/" + currentName;
        int responseCode = sendServerDeleteRequest(namingServerIp, path);
        if (responseCode == 200) {
            System.out.println("DELETE request for '" + currentName + "' successfully sent to " + namingServerIp);

        } else {
            System.err.println("Error: failed to send DELETE request to " + namingServerIp + " — HTTP " + responseCode);

        }
    }
    public void deleteNodeRequestFromHash(int hash){
        String namingServerIp = nodeRepository.getNamingServerIp();
        try {
            URL url = new URL("http://" + namingServerIp + ":"+nodeRepository.getNamingServerHTTPPort() + "/api/node/hash/" + hash);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("DELETE request for hash'" +hash  + "' successfully sent to " + namingServerIp);
            } else {
                System.err.println("Failed to send DELETE request to " + namingServerIp + " — HTTP " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Error DELETE request to " + namingServerIp);
            e.printStackTrace();
        }
    }

    public JSONObject getNeighborsRequest(int currentId){
        String namingServerIp = nodeRepository.getNamingServerIp();
        String path = "/api/node/nb/" + currentId;
        return sendServerGetRequest(namingServerIp,path);
    }


    public JSONObject getServerObjectRequest(String path){
        String namingServerIp = nodeRepository.getNamingServerIp();
        return sendServerGetRequest(namingServerIp,path);
    }

    public static boolean acquireFileLock(String fileName, int nodeId) {
        String path = "/api/lock/acquire";
        JSONObject body = new JSONObject();
        body.put("fileName", fileName);
        body.put("nodeId", nodeId);

        try {
            int responseCode = sendHttpRequest("POST", path, body.toString());
            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("Failed to acquire lock: " + e.getMessage());
            return false;
        }
    }

    public static boolean releaseFileLock(String fileName, int nodeId) {
        String path = "/api/lock/release";
        JSONObject body = new JSONObject();
        body.put("fileName", fileName);
        body.put("nodeId", nodeId);

        try {
            int responseCode = sendHttpRequest("DELETE", path, body.toString());
            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("Failed to release lock: " + e.getMessage());
            return false;
        }
    }

    private static int sendHttpRequest(String method, String path, String jsonBody) throws Exception {
        URL url = new URL("http://"+nodeRepository.getNamingServerIp()+":"+nodeRepository.getNamingServerHTTPPort() + path);
        System.out.println("http://"+nodeRepository.getNamingServerIp()+":"+nodeRepository.getNamingServerHTTPPort()+path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        if (jsonBody != null && !jsonBody.isEmpty()) {
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        return conn.getResponseCode();
    }




}
