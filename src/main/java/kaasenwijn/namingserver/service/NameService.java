package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.repository.IpRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.abs;

@Service
public class NameService {

    /**
     * Implementation of the hashing function as described in the project explanation.
     * It has a bound between 0 and 32 768.
     *
     * @param name the string value to hash
     * @return hash of the name as an integer value between 0 and 32 768
     */
    public Integer getHash(String name){
        double fac = (double) 32768 /((long) 2*Integer.MAX_VALUE);
        long med = (name.hashCode()  + (long) Integer.MAX_VALUE);
        double result = med * fac;
        return (int) Math.floor(result);
    }

    public Integer getNodeId(String filename){
        Integer fileId = getHash(filename);
        boolean isEmpty=true;
        Integer owner = 0;
        Integer largest = 0;
        for(Integer nodeId : IpRepository.getAllIds()){
            if(nodeId > largest){
                largest = nodeId;
            }
            if(nodeId < fileId && nodeId > owner){
                owner = nodeId;
                isEmpty=false;
            }
        }
        return isEmpty ? largest : owner;
    }


}
