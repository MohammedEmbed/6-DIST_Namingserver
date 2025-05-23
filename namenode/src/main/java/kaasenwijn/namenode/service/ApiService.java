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

    private int sendServerPostRequest(String filename, File file, String targetIp, String path){
        try {
            URL url = new URL("http://" + targetIp+":"+nodeRepository.getNamingServerHTTPPort()  + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("File-Name", filename);
            conn.setRequestProperty("Content-Type", "application/octet-stream");

            try (OutputStream os = conn.getOutputStream(); FileInputStream fis = new FileInputStream(file)) {
                fis.transferTo(os);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode;

        }catch(Exception e){
            System.err.println("Error: failed POST request to " + targetIp);
            e.printStackTrace();
            return 500;
        }
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

    public void postFileRequest(String filename,File file, String targetIp){
        String path = "/api/node/files/replicate";
        int responseCode = sendServerPostRequest(filename, file, targetIp, path);
        if (responseCode == 200) {
            System.out.println(filename +"POST request successfully transferred "+ filename +" to "+targetIp);
        } else {
            System.err.println("Error: failed to send POST request of " + filename + " to " + targetIp + " — HTTP " + responseCode);
        }
    }

    public boolean checkFileRequest(String nodeIp, String filename){
        try {
            URL url = new URL("http://" + nodeIp + ":"+nodeRepository.getNamingServerHTTPPort() +"/api/node/files/has/" + filename);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);

            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            System.err.println("Failed to check if node " + nodeIp + " has file " + filename);
            return false;
        }
    }
    private static final String NAMING_SERVER_URL = "http://localhost:8080"; // Adjust to your actual Naming Server address

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
        URL url = new URL(NAMING_SERVER_URL + path);
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
