package kaasenwijn.namenode.util;

import kaasenwijn.namenode.model.Neighbor;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.FileMonitor;
import kaasenwijn.namenode.service.NodeService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static kaasenwijn.namenode.util.Failure.initiateFailureAgent;

public class NodeUnicastReceiver extends Thread {
    private static final int UNICAST_SENDER_PORT = 9090; // Node unicast sender port = flipped t.o.v. nameServer

    private static final NodeRepository nodeRepository = NodeRepository.getInstance();
    private static final Set<Integer> lockedFiles = new HashSet<>();

    @Override
    public void run() {
        try {
            InetAddress bindAddress = InetAddress.getByName(nodeRepository.getSelfIp());
            ServerSocket serverSocket = new ServerSocket(nodeRepository.getSelfPort(), 50, bindAddress);
            System.out.println("Socked opened on: " + nodeRepository.getSelfIp() + ":" + nodeRepository.getSelfPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder messageBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("END")) break;
                    messageBuilder.append(line);
                }

                String message = messageBuilder.toString();
                JSONObject json = new JSONObject(message);
                String type = json.getString("type");
                // Contains ip, port and name of sender
                JSONObject source = json.getJSONObject("source");
                JSONObject data = json.getJSONObject("data");
                switch (type) {
                    case "health-check":
                        // We just want to test if the node is alive, we don't need to respond explicitly
                        break;
                    case "welcome":
                        int nodeCount = data.getInt("nodes");
                        System.out.println("[Welcome] Nodes in system: " + nodeCount);

                        // If count is one, the node is alone in the network
                        if (nodeCount == 1) {
                            nodeRepository.setPrevious(nodeRepository.getCurrentId());
                            nodeRepository.setNext(nodeRepository.getCurrentId());
                        } else {
                            nodeRepository.setPrevious(data.getInt("previousNode"));
                            nodeRepository.setNext(data.getInt("nextNode"));
                        }
                        System.out.println("[welcome] nextid: " + nodeRepository.getNextId() + " , previousid: " + nodeRepository.getPreviousId());

                        // If the count is not 1, it wil receive messages from other nodes to update prev and next id
                        break;

                    case "update_ids":
                        if (!data.isEmpty()) {
                            int nextId = data.getInt("next_id");
                            int previousId = data.getInt("previous_id");
                            nodeRepository.setNext(nextId);
                            nodeRepository.setPrevious(previousId);
                            System.out.println("[update_ids] New nextid: " + nextId + " , new previousid: " + previousId);
                        }
                        break;

                    // For Shutdown
                    case "update_next_id":
                        if (!data.isEmpty()) {
                            int nextId = data.getInt("next_id");
                            nodeRepository.setNext(nextId);
                            System.out.println("[update_next_id] New nextid: " + nextId);
                        }
                        break;

                    case "update_previous_id":
                        if (!data.isEmpty()) {
                            int previousId = data.getInt("previous_id");
                            nodeRepository.setPrevious(previousId);
                            System.out.println("[update_previous_id] New previousid: " + previousId);
                        }
                        break;


                    case "replication_response":
                        int fileHash = data.getInt("fileHash");
                        String filename = FileMonitor.getKnownFiles().get(fileHash);
                        String targetIp = data.getString("ownerIp");
                        int targetPort = data.getInt("ownerPort");
                        if (filename != null) {
                            System.out.println("[replication_response] Received replication response => Send " + filename + " to " + targetIp);
                            NodeSender.sendFile(targetIp,targetPort,filename);
                        } else {
                            System.err.printf("[replication_response] File with hash %d not found in known files.\n", fileHash);
                        }
                        break;

                    case "file_replication": //The node RECEIVES a file from another node to be replicated on it.

                        String fileName = data.getString("fileName");
                        System.out.printf("[file_replication] file %s received from %s : %s \n",fileName,source.getString("ip"),source.getInt("port"));
                        receiveFile(inputStream, fileName);
                        logReplication(fileName,NodeService.getHash(source.getString("name")));
                        break;

                    case "shutdown_replication"://A node that will shut down passes down a replicated file and log
                        String nameofFile = data.getString("fileName");
                        int fileHash2 =data.getInt("fileHash");
                        Neighbor previousNode = nodeRepository.getPrevious();
                        if(FileMonitor.getKnownFiles().containsKey(fileHash2) &!source.getString("ip").equals(previousNode.getIp())) {
                            //Current node has file stored locally -> send it to previous node (unless last node in the system)
                            System.out.println("Edge case: file sent to previous node.");
                            NodeSender.sendFile(previousNode.getIp(), previousNode.getPort(), nameofFile);
                        }else {
                            receiveFile(inputStream, nameofFile);
                            //Update the logfile with the currentOwner
                            JSONObject logData = data.getJSONObject("logFile");
                            JSONObject downloadedInfo = new JSONObject();
                            downloadedInfo.put("node_id", nodeRepository.getCurrentId());
                            JSONArray downloadArray = logData.getJSONArray("downloaded_locations");
                            downloadArray.put(downloadedInfo);
                            logData.put("downloaded_locations", downloadArray);
                            String logFileName = data.getString("logFileName");
                            String logFilePath = "logs_" + nodeRepository.getName() + "/" + logFileName;
                            File logFile = new File(logFilePath);

                            try (FileWriter fileWriter = new FileWriter(logFile);) {
                                fileWriter.write(logData.toString(2));
                                System.out.println("Created replication log: " + logFileName);
                            } catch (IOException e) {
                                System.err.println("Failed to create replication log: " + logFileName);
                                e.printStackTrace();
                            }
                        }
                            break;

                    case "file_replication_deletion":

                        String fileName2 = data.getString("fileName");
                        System.out.printf("[file_replication_deletion] file %s received from %s : %s \n",fileName2,source.getString("ip"),source.getInt("port"));
                        String filePath = "replicated_files_"+nodeRepository.getName()+"/"+fileName2;
                        deleteFile(filePath);
                        String logFilePath = "logs_"+nodeRepository.getName() + "/replication_log_" + NodeService.getHash(fileName2) + ".json";
                        deleteFile(logFilePath);
                        break;

                    case "file_list_sync":
                        System.out.println("[file_list_sync] Received file list from " + source.getString("ip"));

                        HashMap<Integer, String> currentNodeFiles = FileMonitor.getKnownFiles();

                        JSONObject neighborFileMap = data;
                        for (String receivedHashStr : neighborFileMap.keySet()) {
                            int receivedFileHash = Integer.parseInt(receivedHashStr);
                            String receivedFileName = neighborFileMap.getString(receivedHashStr);

                            if (!currentNodeFiles.containsKey(receivedFileHash)) {
                                System.out.printf(" → Missing file detected: %s (hash: %d)%n", receivedFileName, receivedFileHash);
                                // TODO: add request to fetch the file
                            }
                        }
                        break;

                    case "lock_request":
                        int lockRequestHash = data.getInt("fileHash");
                        JSONObject lockResponse = new JSONObject();
                        if (!lockedFiles.contains(lockRequestHash)) {
                            lockedFiles.add(lockRequestHash);
                            lockResponse.put("status", "granted");
                        } else {
                            lockResponse.put("status", "denied");
                        }
                        lockResponse.put("fileHash", lockRequestHash);
                        try {
                            NodeSender.sendUnicastMessage(source.getString("ip"), source.getInt("port"), "lock_response", lockResponse);
                        } catch (CommunicationException e) {
                            System.err.println("[lock_request] Failed to send lock response to " + source.getString("ip"));
                            e.printStackTrace();
                        }
                        break;

                    case "lock_response":
                        String lockStatus = data.getString("status");
                        int lockedFileHash = data.getInt("fileHash");
                        if ("granted".equals(lockStatus)) {
                            System.out.printf("[lock_response] Lock granted for file %d. Ready to download.%n", lockedFileHash);
                            // Handle download trigger logic
                        } else {
                            System.out.printf("[lock_response] Lock denied for file %d. Will retry later.%n", lockedFileHash);
                        }
                        break;


                }

                in.close();
                clientSocket.close();
            }

        } catch (IOException e) {
            System.err.println("Error in UnicastReceiver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void receiveFile(InputStream in, String fileName) throws IOException {

        FileOutputStream fos = new FileOutputStream("replicated_files_"+nodeRepository.getName()+"/"+fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }

        bos.flush();
        bos.close();
        System.out.println("File received successfully!");
    }

    public static void deleteFile(String path) {

       try{

           Files.deleteIfExists(Path.of(path));

               System.out.println("[Delete file] File with name "+path+" was deleted successfully.");

       }catch (Exception e){
           e.printStackTrace();
           System.out.println("[Delete file] File with name "+path+" could not be deleted.");

       }
    }



    /**
     * Create a Log with information about on the file that's replicated
     * The log is created upon reception of a new file
     * @param filename name of the received file
     * @param originalOwnerId hash of the original owner/source of the file
     */
    private void logReplication(String filename, int originalOwnerId) {
        int fileHash = NodeService.getHash(filename);
        String logFileName = "logs_"+nodeRepository.getName() + "/replication_log_" + fileHash + ".json";
        File logFile = new File(logFileName);

        if (!logFile.exists()) {
            JSONObject logData = new JSONObject();
            logData.put("filename", filename);
            logData.put("hash", fileHash);

            JSONObject ownerInfo = new JSONObject();
            ownerInfo.put("node_id", originalOwnerId);
            logData.put("original_owner", ownerInfo);

            JSONArray downloadedInfo = new JSONArray();
            JSONObject downloadedInfoEntry = new JSONObject();
            downloadedInfoEntry.put("node_id", nodeRepository.getCurrentId());
            downloadedInfo.put(downloadedInfoEntry);
            logData.put("downloaded_locations", downloadedInfo);

            try (FileWriter fileWriter = new FileWriter(logFile);) {
                fileWriter.write(logData.toString(2));
                System.out.println("Created replication log: " + logFileName);
            } catch (IOException e) {
                System.err.println("Failed to create replication log: " + logFileName);
                e.printStackTrace();
            }

        } else { // When the logfile already exists and this is just a new download of the file
            try (FileReader fileReader = new FileReader(logFile);) {
                JSONObject jsonObject = new JSONObject(new JSONTokener(fileReader));
                JSONObject newEntry = new JSONObject();
                newEntry.put("node_id",nodeRepository.getCurrentId());
                jsonObject.getJSONArray("downloaded_locations").put(newEntry);

                try (FileWriter fileWriter = new FileWriter(logFile);) {
                    fileWriter.write(jsonObject.toString(2));
                    System.out.println("Created replication log: " + logFileName);
                } catch (IOException e) {
                    System.err.println("Failed to create replication log: " + logFileName);
                    e.printStackTrace();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Replication log already exists for file with hash: " + fileHash);

        }
    }


}
