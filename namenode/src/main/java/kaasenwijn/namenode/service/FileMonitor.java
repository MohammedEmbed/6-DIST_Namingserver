package kaasenwijn.namenode.service;

import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.CommunicationException;
import kaasenwijn.namenode.util.NodeSender;
import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FileMonitor extends Thread {

    private final Set<String> knownFiles = new HashSet<>();
    private final File folder = new File("local_files_"+NodeRepository.getInstance().getName());

    @Override
    public void run() {
        System.out.println("FileMonitor started for folder: " + folder.getAbsolutePath());

        while (true) {
            try {
                if (folder.exists() && folder.isDirectory()) { // Does the folder exist?
                    File[] files = folder.listFiles(); // List all files
                    if (files != null) { // Are there any files?
                        for (File file : files) { // Loop through all files
                            if (file.isFile() && !knownFiles.contains(file.getName())) { // Is this a new file?
                                String filename = file.getName();
                                knownFiles.add(filename);

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
                    }
                }

                Thread.sleep(100); // Check for new files every 100 milliseconds

            } catch (InterruptedException e) {
                System.err.println("FileMonitor interrupted.");
                break;
            }
        }
    }
}
