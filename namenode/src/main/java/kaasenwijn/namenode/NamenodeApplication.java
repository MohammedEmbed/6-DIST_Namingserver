package kaasenwijn.namenode;

import kaasenwijn.namenode.service.NodeMulticastListener;
import kaasenwijn.namenode.service.NodeService;
import kaasenwijn.namenode.service.UnicastReceiver;
import kaasenwijn.namenode.util.MulticastSender;
import kaasenwijn.namenode.service.NodeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class NamenodeApplication {

	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		String ip = System.getenv("SERVER_IP");
		int port = Integer.parseInt(System.getenv("SERVER_PORT"));
		String hostName = System.getenv("SERVER_NAME");
		System.out.println("Node started with IP-address: "+ip+", Port: "+port+" and Name: "+hostName);

		NodeService.startUp(ip, hostName);

		// Start listening for unicasts
		UnicastReceiver receiver = new UnicastReceiver(ip,port);
		receiver.start();

		// Start listening for multicasts
		NodeMulticastListener listener = new NodeMulticastListener();
		listener.start();


		// Start sending multicasts
		// Add a little delay to give everybody time to start-up
		Thread.sleep(5000);
		for(int i=0;i<5;i++){
			MulticastSender.sendMulticastMessage(hostName,ip,port);
		}
	}

}
