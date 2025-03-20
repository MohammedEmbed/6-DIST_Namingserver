package kaasenwijn.namingserver.repository;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

public class IpRepository {
    private final Map<Integer, String> ipMap = new HashMap<>(); //[Integer, Ip Address] => node
    private static IpRepository single_instance = null;

    @Value("${repository.filename}")
    private String fileName;
    private IpRepository()
    {
        this.readJson();
    }

    private static synchronized IpRepository getInstance()
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
}
