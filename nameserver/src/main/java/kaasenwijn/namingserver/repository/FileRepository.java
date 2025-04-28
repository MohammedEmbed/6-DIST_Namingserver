package kaasenwijn.namingserver.repository;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileRepository {

    private static final String FILE_PATH = "file_ownership.json"; //TODO Change path
    private static FileRepository instance = null;
    private final Map<Integer, String> fileOwnership = new HashMap<>(); // fileHash → owner IP

    private FileRepository() {
        loadFromDisk();
    }

    public static synchronized FileRepository getInstance() {
        if (instance == null) {
            instance = new FileRepository();
        }
        return instance;
    }

    public synchronized void register(int fileHash, String ownerIp) {
        fileOwnership.put(fileHash, ownerIp);
        saveToDisk();
    }

    public synchronized String getOwner(int fileHash) {
        return fileOwnership.get(fileHash);
    }

    public synchronized boolean contains(int fileHash) {
        return fileOwnership.containsKey(fileHash);
    }

    public synchronized Map<Integer, String> getAll() {
        return new HashMap<>(fileOwnership);
    }

    private void saveToDisk() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            JSONObject json = new JSONObject();
            for (Map.Entry<Integer, String> entry : fileOwnership.entrySet()) {
                json.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            writer.write(json.toString(2)); // Pretty print
            /// Example:
            /// Makes JSON go from:
            /// {"7":"127.0.0.1","19":"127.0.0.2"}

            /// To:
            /// {
            ///   "7": "127.0.0.1",
            ///   "19": "127.0.0.2"
            ///  }
        } catch (IOException e) {
            System.err.println("Failed to save file ownership map.");
            e.printStackTrace();
        }
    }

    private void loadFromDisk() {
        File file = new File(FILE_PATH);
        if (!file.exists()) { //If file_ownership.json isn't found or there isn't one yet
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JSONObject json = new JSONObject(new JSONTokener(reader));
            for (String key : json.keySet()) {
                int fileHash = Integer.parseInt(key);
                String ownerIp = json.getString(key);
                fileOwnership.put(fileHash, ownerIp);
            }
        } catch (IOException e) {
            System.err.println("Failed to load file ownership map.");
            e.printStackTrace();
        }
    }
}
