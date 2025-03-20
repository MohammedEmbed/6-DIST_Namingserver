package kaasenwijn.namingserver;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

// TODO: make this a singleton + thread safe
public class IpRepository {
    private final Map<Integer, String> map = new HashMap<>(); //[Integer, Ip Address] => node

    public IpRepository() {
        this.readJson();
    }

    public String getIp(int id) {
        return map.getOrDefault(id, "ip doesn't exist");

    }

    public void setIp(Integer id, String ip) {
        this.map.put(id,ip);
        this.writeJson();
    }

    private void readJson(){
        // TODO: read json file into the hashset
    }

    private void writeJson() {
        // TODO: write hashmap to json
        JSONObject jsonObject = new JSONObject(map);
        String orgJsonData = jsonObject.toString();
    }


}
