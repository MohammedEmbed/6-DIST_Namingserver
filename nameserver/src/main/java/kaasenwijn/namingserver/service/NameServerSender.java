package kaasenwijn.namingserver.service;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.Socket;

public class NameServerSender {
    private static final int UNICAST_RECEIVE_PORT = 8081; // Node unicast listener port

    public static void unicastSend(String sourceNodeIp, String targetNodeIp, String filename){
        try (Socket socket = new Socket(sourceNodeIp, UNICAST_RECEIVE_PORT)) {
            JSONObject msg = new JSONObject();
            msg.put("type", "replication_request");
            msg.put("file", filename);
            msg.put("to", targetNodeIp);

            OutputStream out = socket.getOutputStream();
            out.write(msg.toString().getBytes());
            out.flush();

            System.out.printf("Sent replication request to %s → Send '%s' to %s%n",
                    sourceNodeIp, filename, targetNodeIp);

        } catch (Exception e) {
            System.err.printf("Failed to send replication request to %s for file %s%n",
                    sourceNodeIp, filename);
            e.printStackTrace();
        }
    }
}
