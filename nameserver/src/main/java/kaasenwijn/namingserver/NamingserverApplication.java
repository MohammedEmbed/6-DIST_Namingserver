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
        String ip = InetAddress.getLocalHost().getHostAddress();
        int port = 8090;
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
