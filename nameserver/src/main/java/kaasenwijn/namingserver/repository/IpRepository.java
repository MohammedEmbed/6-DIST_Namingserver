package kaasenwijn.namingserver.repository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONObject;
import org.json.JSONTokener;

public class IpRepository {
    private final HashMap<Integer, String> ipMap = new HashMap<>(); //[Integer, Ip Address] => node
    private static IpRepository single_instance = null;

    private final String fileName = "database.json";

    private IpRepository() {
        this.readJson();
    }

    public static synchronized IpRepository getInstance() {
        if (single_instance == null)
            single_instance = new IpRepository();

        return single_instance;
    }

    public String getIp(int id) {
        return this.ipMap.getOrDefault(id, "ip doesn't exist");
    }

    public int getNextId(int hash) {
        // https://stackoverflow.com/a/2657015
        // Create a TreeMap to sort the entries by key
        TreeMap<Integer, String> sortedMap = new TreeMap<>(this.ipMap);

        // Find the entry with the current key
        Map.Entry<Integer, String> currentEntry = sortedMap.ceilingEntry(hash);
        if (currentEntry == null) {
            return 0; // Current key not found in the map
        }

        // Get the next entry
        Map.Entry<Integer, String> nextEntry = sortedMap.higherEntry(hash);
        if (nextEntry == null) {
            return sortedMap.firstKey().hashCode();
        }

        // Return the hash code of the next entry's key
        return nextEntry.getKey().hashCode();
    }

    public int getPreviousId(int id) {
        // https://stackoverflow.com/a/2657015
        // Create a TreeMap to sort the entries by key
        TreeMap<Integer, String> sortedMap = new TreeMap<>(this.ipMap);

        // Find the entry with the current key
        Map.Entry<Integer, String> currentEntry = sortedMap.ceilingEntry(id);
        if (currentEntry == null) {
            return 0; // Current key not found in the map
        }

        // Get the next entry
        Map.Entry<Integer, String> prevEntry = sortedMap.lowerEntry(id);
        if (prevEntry == null) {
            return sortedMap.lastKey().hashCode();
        }

        // Return the hash code of the next entry's key
        return prevEntry.getKey().hashCode();
    }

    public boolean ipExists(int id) {
        return this.ipMap.containsKey(id);
    }

    public void setIp(Integer id, String ip) {
        if (!ipExists(id)) {
            this.ipMap.put(id, ip);
            this.writeJson();
        }
    }

    private void readJson() {
        File file = new File(this.fileName);
        if (!file.exists()) {
            System.out.println("File not found: ");
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            // Parse JSON
            JSONObject jsonObject = new JSONObject(new JSONTokener(reader));

            for (String key : jsonObject.keySet()) {
                this.ipMap.put(Integer.parseInt(key), jsonObject.getString(key));
            }
            System.out.println("Parsed JSON to HashSet");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeJson() {
        JSONObject jsonObject = new JSONObject(ipMap);
        String orgJsonData = jsonObject.toString();
        // Write JSON to file
        try (FileWriter file = new FileWriter(this.fileName)) {
            file.write(orgJsonData);
            System.out.println("JSON file created successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, String> getMap() {
        return this.ipMap;
    }

    public static Set<Integer> getAllIds() {
        return getInstance().getMap().keySet();
    }

    public void remove(Integer id) {
        this.ipMap.remove(id);
    }

    public static void printRegisteredNodes() {
        IpRepository repo = IpRepository.getInstance();

        System.out.println("==== Registered Nodes ====");
        if (repo.getMap().isEmpty()) {
            System.out.println("No nodes registered.");
        } else {
            repo.getMap().forEach((name, ip) -> {
                System.out.println("Node: " + name + " | IP: " + ip);
            });
        }
        System.out.println("==========================");
    }
}
