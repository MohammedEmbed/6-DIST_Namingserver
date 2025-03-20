package kaasenwijn.namingserver.service;

import org.springframework.stereotype.Service;

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
        return (name.hashCode() + Integer.MAX_VALUE) * (32768/Integer.MAX_VALUE +abs(Integer.MIN_VALUE));
    }
}
