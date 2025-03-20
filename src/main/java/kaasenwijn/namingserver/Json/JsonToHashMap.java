package kaasenwijn.namingserver.Json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class JsonToHashMap {
    public static void main(String[] args) {
        String json = "{ \"1\": \"Apple\", \"2\": \"Banana\", \"3\": \"Cherry\", \"4\": \"Date\", \"5\": \"Elderberry\" }";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<Integer, String> map = objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(HashMap.class, Integer.class, String.class));

            System.out.println("Parsed HashMap: " + map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
