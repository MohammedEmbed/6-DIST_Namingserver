package kaasenwijn.namenode.service;

import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.util.NodeSender;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Objects;

@Service
public class NodeService {

    private final static NodeRepository nodeRepository = NodeRepository.getInstance();
    public static void startUp(String ip,int port,String name){
        Integer id = getHash(name);
        NodeRepository repo = NodeRepository.getInstance();
        repo.setCurrentId(id);
        repo.setSelfIp(ip);
        repo.setSelfPort(port);
        repo.setName(name);
        repo.setNextId(id);
        repo.setPreviousId(id);

        // TODO: fix for lab5, currently makes the server crash
        // verifyLocalFiles();
        // reportLocalFilesToNamingServer();
    }
    //The same hash function as the Namingserver:
    public static Integer getHash(String name){
        double fac = (double) 32768 /((long) 2*Integer.MAX_VALUE);
        long med = (name.hashCode()  + (long) Integer.MAX_VALUE);
        double result = med * fac;
        return (int) Math.floor(result);
    }

    public static JSONObject updateNeighborsData(int hash){
        int currentId = nodeRepository.getCurrentId();

        int previousId = nodeRepository.getPreviousId();
        int nextId = nodeRepository.getNextId();

        JSONObject data = new JSONObject();

        // When network exists of one node and another joins
        if(currentId == nextId && currentId == previousId){
            nodeRepository.setNextId(hash);
            nodeRepository.setPreviousId(hash);
            // Data to send in unicast to node to say that it's between this node and the nextid of this node
            data.put("previous_id",nodeRepository.getCurrentId());
            data.put("next_id",nodeRepository.getCurrentId());
        }

        if(currentId < hash && hash < nextId){
            nodeRepository.setNextId(hash);
            // Data to send in unicast to node to say that it's between this node and the nextid of this node
            data.put("previous_id",nodeRepository.getCurrentId());
            data.put("next_id",hash);


        }else if(previousId < hash && hash < currentId){
            nodeRepository.setPreviousId(hash);
            // Data to send in unicast to node to say that it's between the previous node and this node
            data.put("previous_id",hash);
            data.put("next_id",nodeRepository.getCurrentId());
        }
        return data;
    }

    public static void verifyLocalFiles(){
        /*
        De files voor elke node moeten momenteel bijgehouden worden in het mapje files.
        de folder (lijn hier onder) kijkt op die map en lijst de files op die erin zetten.

        Dan extra is nog kort alle local files printen (ter info mag eigenlijk weg)
         */
        File folder = new File("files");
        File[] files = folder.listFiles();

        //Print out local files
        System.out.println("Local files:");
        for (File file : files) {
            if (file.isFile()) {
                System.out.println("- " + file.getName());
            }
        }
    }

    public static void reportLocalFilesToNamingServer() {
        File folder = new File(System.getProperty("user.dir") + File.separator + "files");
        if (!folder.exists()) return;

        NodeRepository repo = NodeRepository.getInstance(); //Instantie opvragen
        String selfIp = repo.getSelfIp(); //Vraag Ip op

        for (File file : folder.listFiles()) { //Lijst alle files van de node op
            if (!file.isFile()) continue;

            String filename = file.getName();
            int fileHash = getHash(filename); //Bereken hun hashes


        }
    }

    public static boolean shouldDrop(JSONObject packet){
        JSONObject source = packet.getJSONObject("source");
        return Objects.equals(nodeRepository.getSelfIp(), source.getString("ip"));

    }

}
