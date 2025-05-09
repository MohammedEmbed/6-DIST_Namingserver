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
        JSONObject json = apiService.getNodeIpRequest(this.Id);

        if (json != null) {
            return json.getString("ip");
        } else {
            throw new RuntimeException();
        }

    }

}
