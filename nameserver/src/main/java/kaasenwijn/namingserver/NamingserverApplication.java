package kaasenwijn.namingserver;

import kaasenwijn.namingserver.repository.IpRepository;
import kaasenwijn.namingserver.service.NameServerMulticastListener;
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
        String ip = InetAddress.getLocalHost().getHostAddress();
        int port = 8080;
        String hostName = "NamingServer";
        System.out.println("Node started with IP-address: "+ip+", Port: "+port+" and Name: "+hostName);

        // Start listening for multicasts
        NameService nameService = new NameService(); // or get it from Spring
        NameServerMulticastListener listener = new NameServerMulticastListener(nameService);
        listener.start();

        // Print node info
        IpRepository.printRegisteredNodes();
    }

}
