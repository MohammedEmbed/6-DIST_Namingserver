package kaasenwijn.namenode.util;

import java.net.InetAddress;

public class Failure {

    public boolean ping(String ip){
        // https://stackoverflow.com/a/29460716
        try{
            InetAddress address = InetAddress.getByName(ip);
            boolean reachable = address.isReachable(1000);

            System.out.println("Is host reachable? " + reachable);
            return reachable;
        } catch (Exception e){
            ExecuteFailure(ip);
            e.printStackTrace();
        }
        ExecuteFailure(ip);
        return false;
    }

    /**
     *  This algorithm is activated in every exception thrown during communication
     *   with other nodes. This allows distributed detection of node failure<br/><br/>
     *  Request the previous node and next node parameters from the nameserver<br/>
     *  Update the `next node` parameter of the previous node with the information
     *   received from the nameserver<br/>
     *  Update the `previous node` parameter of the next node with the information
     *   received from the nameserver<br/>
     *  Remove the node from the Naming server<br/>
     *  Test this algorithm manually terminating a node (CTRL – C) and use a ping
     *   method as part of each node, that throws an exception when connection fails
     *   to a given node
     * @param ip
     */
    public void ExecuteFailure(String ip){

    }


}
