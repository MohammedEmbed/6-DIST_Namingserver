package kaasenwijn.namenode.service;

import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.CommunicationException;
import kaasenwijn.namenode.util.NodeSender;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileMonitor extends Thread {

    private  final HashMap<Integer,String>  knownFiles = new HashMap<Integer,String>();
    private final File folder = new File("local_files_"+NodeRepository.getInstance().getName());

    @Override
    public void run() {
        System.out.println("FileMonitor started for folder: " + folder.getAbsolutePath());

        while (true) {
            try {
                HashMap<Integer,Boolean> paintMap = new HashMap<>();
                for(int key : knownFiles.keySet()){
                    paintMap.put(key, false);
                }

                if (folder.exists() && folder.isDirectory()) { // Does the folder exist?
                    File[] files = folder.listFiles(); // List all files
                    if (files != null) { // Are there any files?
                        for (File file : files) { // Loop through all files
                            int fileHash = NodeService.getHash(file.getName());
                            paintMap.put(fileHash, true);
                            if (file.isFile() && !knownFiles.containsKey(fileHash)) { // Is this a new file?
                                String filename = file.getName();
                                knownFiles.put(fileHash,filename);

                                System.out.println("Detected new file: " + filename);

                                // Prepare data to send
                                JSONObject data = new JSONObject();
                                data.put("filename", filename);
                                data.put("fileHash", NodeService.getHash(filename));
                                data.put("nodeHash", NodeRepository.getInstance().getCurrentId());

                                // Send replication request to the naming server
                                try {
                                    NodeSender.sendUnicastMessage(
                                            NodeRepository.getInstance().getNamingServerIp(),
                                            NodeRepository.getInstance().getNamingServerPort(),
                                            "replication",
                                            data
                                    );
                                    System.out.printf("[File monitor] send replication request by node for file %s \n", filename);
                                } catch (CommunicationException e) {
                                    System.err.println("Failed to send replication request for file: " + filename);
                                    e.printStackTrace();
                                }
                            }

                        }
                        // Send delete request for each not painted file
                        for (int key: paintMap.keySet()) {
                            if(!paintMap.get(key)){
                                // New location
                                System.out.println("[File monitor] Deleted file discover: send replication delete request");
                                JSONObject data = NodeService.getFileReplicationLocation(key);
                                JSONObject toSendData = new JSONObject();
                                toSendData.put("fileName",knownFiles.get(key));
                                NodeSender.sendUnicastMessage(data.getString("ip"), data.getInt("port"),"file_replication_deletion", toSendData);
                                knownFiles.remove(key);
                            }
                        }
                    }
                }

                Thread.sleep(10000); // Check for new files every 100 milliseconds

            } catch (InterruptedException e) {
                System.err.println("FileMonitor interrupted.");
                break;
            } catch (CommunicationException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static HashMap<Integer,String> getKnownFiles() {
        return FileMonitorHolder.INSTANCE.knownFiles;
    }

    public static FileMonitor getInstance() {
        return FileMonitorHolder.INSTANCE;
    }

    // Singleton holder pattern
    private static class FileMonitorHolder {
        private static final FileMonitor INSTANCE = new FileMonitor();
    }
}
