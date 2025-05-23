package kaasenwijn.namingserver;

import kaasenwijn.namingserver.repository.IpRepository;
import kaasenwijn.namingserver.service.NameServerMulticastReceiver;
import kaasenwijn.namingserver.service.NameServerUnicastReceiver;
import kaasenwijn.namingserver.service.NameService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class NamingserverApplication {

    public static void main(String[] args) throws UnknownHostException {
        // Start web server
        SpringApplication.run(NamingserverApplication.class, args);
        String ip;
        boolean isRemote = Boolean.parseBoolean(System.getProperty("REMOTE"));
        int port;
        if (isRemote){
            port = Integer.parseInt(System.getProperty("NS_PORT"));
            ip = InetAddress.getLocalHost().getHostAddress();
        } else{
            System.out.println("not remote yess");
            port = 8090;
            ip = "127.0.0.1";
        }
        String hostName = "NamingServer";
        System.out.println("Node started with IP-address: "+ip+", Port: "+port+" and Name: "+hostName);
        NameService.startUp(ip,port,hostName);

        // Start listening for unicast
        NameServerUnicastReceiver receiver = new NameServerUnicastReceiver();
        receiver.start();

        // Start listening for multicasts
        NameServerMulticastReceiver listener = new NameServerMulticastReceiver();
        listener.start();

        // Print node info
        IpRepository.printRegisteredNodes();
    }

}
