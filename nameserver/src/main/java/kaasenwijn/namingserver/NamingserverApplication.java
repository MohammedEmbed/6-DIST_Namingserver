package kaasenwijn.namingserver;

import kaasenwijn.namingserver.service.NameService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class NamingserverApplication {

    public static void main(String[] args) throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Node started with IP-address: "+ip);
        SpringApplication.run(NamingserverApplication.class, args);

        NameService nameService = new NameService(); // or get it from Spring
        NameServerMulticastListener listener = new NameServerMulticastListener(nameService);
        listener.start();

        // Print node info
        IpRepository.printRegisteredNodes();
    }

}
