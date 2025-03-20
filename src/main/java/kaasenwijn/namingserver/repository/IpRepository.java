package kaasenwijn.namingserver.repository;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Value;

public class IpRepository {
    private final HashMap<Integer, String> ipMap = new HashMap<>(); //[Integer, Ip Address] => node
    private static IpRepository single_instance = null;

    @Value("${repository.filename}")
    private String fileName;
    private IpRepository()
    {
        this.readJson();
    }

    public static synchronized IpRepository getInstance()
    {
        if (single_instance == null)
            single_instance = new IpRepository();

        return single_instance;
    }

    public String getIp(int id) {
        return this.ipMap.getOrDefault(id, "ip doesn't exist");

    }

    public void setIp(Integer id, String ip) {
        this.ipMap.put(id,ip);
        this.writeJson();
    }

    private void readJson(){
        // TODO: read json file into the hashset
        try (FileReader reader = new FileReader(this.fileName)) {
            // Parse JSON
            JSONObject jsonObject = new JSONObject(new JSONTokener(reader));

            for (String key : jsonObject.keySet()) {
                this.ipMap.put(Integer.parseInt(key),jsonObject.getString(key));
            }
            System.out.println("Parsed JSON to HashSet");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeJson() {
        // TODO: write hashmap to json
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

    public HashMap<Integer, String> getMap(){
        return this.ipMap;
    }
    public static Set<Integer> getAllIds(){
        return getInstance().getMap().keySet();
    }

    public void remove(Integer id){
        this.ipMap.remove(id);
    }
}
