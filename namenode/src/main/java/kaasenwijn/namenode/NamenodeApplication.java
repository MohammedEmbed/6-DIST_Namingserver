package kaasenwijn.namenode;

import kaasenwijn.namenode.util.MulticastSender;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class NamenodeApplication {

	public static void main(String[] args) throws UnknownHostException {
		String ip = InetAddress.getLocalHost().getHostAddress();
		System.out.println("Node started with IP-address: "+ip);
		String containerName = System.getenv("CONTAINER_NAME");
		System.out.println("Host name: "+containerName);
		NodeService.startUp(ip, containerName);
		SpringApplication.run(NamenodeApplication.class, args);


	}

}
