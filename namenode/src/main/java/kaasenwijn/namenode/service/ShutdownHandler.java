package kaasenwijn.namenode.service;

import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShutdownHandler {

    private static final NodeRepository repo = NodeRepository.getInstance();
    private static final File REPLICATION_LOG = new File("replication_log.txt"); // TODO: Eventueel naar persistent storage veranderen, Map<> mss?
    private final static ApiService apiService = new ApiService();

    public static void transferReplicatedFilesOnShutdown() {
        if (!REPLICATION_LOG.exists()) {
            System.out.println("No replication_log.txt found — nothing to transfer.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(REPLICATION_LOG))) {
            String line;
            while ((line = reader.readLine()) != null) { //While we can read things out of the log
                String filename = line.trim();
                if (filename.isEmpty()) continue;

                int fileHash = NodeService.getHash(filename);

                // Start with previous node
                Neighbor prev = repo.getPrevious();
                String targetIp = prev.getIp();

                if (nodeHasFileLocally(targetIp, filename)) {
                    System.out.printf(" Previous node (%s) already has '%s'. Finding next fallback...%n", targetIp, filename);
                    Neighbor prevPrev = new Neighbor(NodeService.getNeighbors(prev.Id).getJSONObject("previous").getInt("id"));
                    targetIp = prevPrev.getIp();
                }

                sendFileToNode(filename, targetIp);
            }

        } catch (IOException e) {
            System.err.println("Error reading replication_log.txt");
            e.printStackTrace();
        }
    }

    private static boolean nodeHasFileLocally(String nodeIp, String filename) {
        return apiService.checkFileRequest(nodeIp, filename);
    }

    private static void sendFileToNode(String filename, String targetIp) {
        try {
            File file = new File("files", filename);
            if (!file.exists()) {
                System.err.println("File not found: " + filename);
                return;
            }
            apiService.postFileRequest(filename,file, targetIp);




        } catch (Exception e) {
            System.err.println("Error sending file " + filename + " to " + targetIp);
            e.printStackTrace();
        }
    }
}
