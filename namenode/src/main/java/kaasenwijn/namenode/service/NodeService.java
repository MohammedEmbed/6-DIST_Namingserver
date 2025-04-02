package kaasenwijn.namenode.service;

import kaasenwijn.namenode.repository.NodeRepository;
import org.springframework.stereotype.Service;

@Service
public class NodeService {

    //The same hash function as the Namingserver:
    public Integer getHash(String name){
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


}
