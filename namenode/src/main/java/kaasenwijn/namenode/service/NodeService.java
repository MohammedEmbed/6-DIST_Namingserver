package kaasenwijn.namenode.service;

import kaasenwijn.namenode.repository.NodeRepository;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


import java.io.File;

@Service
public class NodeService {

    public static void startUp(String ip,String name){
        Integer id = getHash(name);
        NodeRepository repo = NodeRepository.getInstance();
        repo.setCurrentId(id);
        repo.setSelfIp(ip);

        verifyLocalFiles();
        reportLocalFilesToNamingServer();
    }
    //The same hash function as the Namingserver:
    public static Integer getHash(String name){
        double fac = (double) 32768 /((long) 2*Integer.MAX_VALUE);
        long med = (name.hashCode()  + (long) Integer.MAX_VALUE);
        double result = med * fac;
        return (int) Math.floor(result);
    }

    public void updateNeighbors(int hash){
        NodeRepository nodeRepo = NodeRepository.getInstance();
        int currentId = nodeRepo.getCurrentId();

        int previousId = nodeRepo.getPreviousId();
        int nextId = nodeRepo.getNextId();

        if(currentId < hash && hash < nextId){
            nodeRepo.setNextId(hash);
            //TODO: send response to next
        }else if(previousId < hash && hash < currentId){
            nodeRepo.setPreviousId(hash);
            //TODO: send response to previous
        }
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

}
