package kaasenwijn.namenode.model;

import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.ApiService;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Neighbor {
    private final static NodeRepository nodeRepository = NodeRepository.getInstance();
    private final static ApiService apiService = new ApiService();
    public Integer Id;


    /**
     * @param Id hash of the neighbouring node
     */
    public Neighbor(Integer Id) {
        this.Id = Id;

    }


    /**
     * Sends a request to the Nameserver to retrieve the IP of the node.
     *
     * @return Node ip
     */
    public String getIp() {
        String[] ipAndPort = this.getIpFromNS().split(":");
        return ipAndPort[0];
    }

    /**
     * Sends a request to the Nameserver to retrieve the port of the node.
     *
     * @return int Node port
     */
    public int getPort() {
        String[] ipAndPort = this.getIpFromNS().split(":");
        return Integer.parseInt(ipAndPort[1]);
    }

    /**
     * Sends a request to the Nameserver to retrieve the ip and port of the node.
     *
     * @return string "ipaddress:port"
     */
    private String getIpFromNS() {
        // Send HTTP GET request to nameserver to receive ip of the node
        apiService.get
        String namingServerIp = nodeRepository.getNamingServerIp();
        System.out.println("GET request for '" + this.Id + "' to " + namingServerIp);
        try {
            URL url = new URL("http://" + namingServerIp + ":8080/api/node/ip/" + this.Id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("GET request for '" + this.Id + "' successfully sent to " + namingServerIp);
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject json = new JSONObject(response.toString());
                return json.getString("ip");
            } else {
                System.err.println("Failed to send GET request to " + namingServerIp + " — HTTP " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error GET request to " + namingServerIp);
            e.printStackTrace();
        }
        return null;
    }
}
