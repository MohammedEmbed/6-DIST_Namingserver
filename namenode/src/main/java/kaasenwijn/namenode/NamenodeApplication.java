package kaasenwijn.namenode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class NamenodeApplication {

	public static void main(String[] args) throws UnknownHostException {
		String ip = InetAddress.getLocalHost().getHostAddress();
		System.out.println("Node started with IP-address: "+ip);
		SpringApplication.run(NamenodeApplication.class, args);
	}

}
