package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.NodeRepository;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.Socket;

public class NameServerSender {

    private static final NodeRepository nodeRepository = NodeRepository.getInstance();
    public static void sendUnicastMessage(String ip,int port, String type, JSONObject data) {
        try(Socket socket = new Socket(ip, port)){
            JSONObject unicastMessageObj = createObject(type,data);
            byte[] buf = unicastMessageObj.toString().getBytes(); // the message we want to send turned into bytes

            OutputStream out = socket.getOutputStream();
            out.write(buf);
            out.flush();

            System.out.println(" Unicast message sent: ip="+ip+", port="+port+", type="+type);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static final int UNICAST_RECEIVE_PORT = 8081; // Node unicast listener port

    // TODO: lab5
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

    private static JSONObject createObject(String type){ //Method overload
        // Create JSON message with name and ip
        JSONObject messageObj = new JSONObject();
        messageObj.put("type", type);
        messageObj.put("source",createSourceObject());
        return messageObj;
    }

    private static JSONObject createObject(String type,JSONObject data){ //Method overload
        // Create JSON message with name and ip
        JSONObject messageObj = createObject(type);
        messageObj.put("data",data);
        return messageObj;
    }

    private static JSONObject createSourceObject(){
        // Create JSON message with name and ip
        JSONObject messageObj = new JSONObject();
        messageObj.put("name", nodeRepository.getName());
        messageObj.put("ip", nodeRepository.getSelfIp());
        messageObj.put("port",nodeRepository.getSelfPort());
        return messageObj;
    }
}
