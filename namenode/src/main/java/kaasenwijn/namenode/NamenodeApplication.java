package kaasenwijn.namenode;

import kaasenwijn.namenode.service.NodeService;
import kaasenwijn.namenode.util.NodeMulticastReceiver;
import kaasenwijn.namenode.util.NodeSender;
import kaasenwijn.namenode.util.NodeUnicastReceiver;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;

@SpringBootApplication
public class NamenodeApplication {

	public static void main(String[] args) throws InterruptedException {
		String ip = System.getenv("SERVER_IP");
		int port = Integer.parseInt(System.getenv("SERVER_PORT"));
		String hostName = System.getenv("SERVER_NAME");
		System.out.println("Node started with IP-address: "+ip+", Port: "+port+" and Name: "+hostName);

		NodeService.startUp(ip, port, hostName);

		// Start listening for unicasts
		NodeUnicastReceiver receiver = new NodeUnicastReceiver();
		receiver.start();

		// Start listening for multicasts
		NodeMulticastReceiver listener = new NodeMulticastReceiver();
		listener.start();

		// Start sending multicasts
		// Add a little delay to give everybody time to start-up
		Random rand = new Random();
		Thread.sleep(4500 + rand.nextInt(500));
		NodeSender.sendMulticastMessage("bootstrap");
		Thread.sleep( rand.nextInt(500));
		NodeSender.sendMulticastMessage("bootstrap");
		Thread.sleep( rand.nextInt(500));
		NodeSender.sendMulticastMessage("bootstrap");
		Thread.sleep( rand.nextInt(500));
		NodeSender.sendMulticastMessage("bootstrap");

		// Register a Shutdown hook
		// https://www.baeldung.com/jvm-shutdown-hooks
		Thread shutdownHook = new Thread(() -> NodeService.shutdown());
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

}
