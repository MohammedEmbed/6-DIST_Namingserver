package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.model.Neighbours;
import kaasenwijn.namingserver.model.NodeIp;
import kaasenwijn.namingserver.repository.IpRepository;
import kaasenwijn.namingserver.repository.NodeRepository;
import org.springframework.stereotype.Service;

@Service
public class NameService {

    private final static IpRepository ipRepo = IpRepository.getInstance();

    /**
     * Implementation of the hashing function as described in the project explanation.
     * It has a bound between 0 and 32 768.
     *
     * @param name the string value to hash
     * @return hash of the name as an integer value between 0 and 32 768
     */
    public static Integer getHash(String name){
        double fac = (double) 32768 /((long) 2*Integer.MAX_VALUE);
        long med = (name.hashCode()  + (long) Integer.MAX_VALUE);
        double result = med * fac;
        return (int) Math.floor(result);
    }

    public static Integer getNodeId(Integer fileHash){
        boolean isEmpty=true;
        Integer owner = 0;
        Integer largest = 0;
        for(Integer nodeId : IpRepository.getAllIds()){
            if(nodeId > largest){
                largest = nodeId;
            }
            if(nodeId < fileHash && nodeId > owner){
                owner = nodeId;
                isEmpty=false;
            }
        }
        return isEmpty ? largest : owner;
    }

    public static void startUp(String ip,int port,String name){
        Integer id = getHash(name);
        NodeRepository repo = NodeRepository.getInstance();
        repo.setCurrentId(id);
        repo.setSelfIp(ip);
        repo.setSelfPort(port);
        repo.setName(name);
    }


    public static Neighbours getNeighbours(int id){
        int nextId = ipRepo.getNextId(id);
        int prevId = ipRepo.getPreviousId(id);
        String nextIp = ipRepo.getIp(nextId);
        String prevIp = ipRepo.getIp(prevId);
        return new Neighbours(new NodeIp(nextId,nextIp), new NodeIp(prevId,prevIp));


    }

}
