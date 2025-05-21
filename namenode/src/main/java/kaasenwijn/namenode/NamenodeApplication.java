package kaasenwijn.namenode;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import kaasenwijn.namenode.repository.NodeRepository;
import kaasenwijn.namenode.service.FileMonitor;
import kaasenwijn.namenode.service.NodeService;
import kaasenwijn.namenode.util.Failure;
import kaasenwijn.namenode.util.NodeMulticastReceiver;
import kaasenwijn.namenode.util.NodeSender;
import kaasenwijn.namenode.util.NodeUnicastReceiver;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import jade.core.*;
import jade.core.Runtime;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class NamenodeApplication {

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        SpringApplication.run(NamenodeApplication.class, args);
        boolean isRemote = Boolean.parseBoolean(System.getProperty("REMOTE"));
        String ip;
        int httpPort;
        if (isRemote){
             ip = InetAddress.getLocalHost().getHostAddress();
             httpPort = Integer.parseInt(System.getProperty("NS_HTTP_PORT"));
        } else{
            ip = System.getProperty("SERVER_IP");
            httpPort = 8091;
        }
        int port = Integer.parseInt(System.getProperty("SERVER_PORT"));

        String hostName = System.getProperty("SERVER_NAME");
        System.out.println("Node started with IP-address: " + ip + ", Port: " + port + ",http port: "+httpPort+ " and Name: " + hostName);

        NodeService.startUp(ip, port,httpPort ,hostName);

        // Initialize JADE container
        Runtime rt = jade.core.Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, hostName);
        profile.setParameter(Profile.MAIN_HOST, ip);
        profile.setParameter(Profile.MAIN_PORT, String.valueOf(port+1));
        // Store container to be used for the FailureAgent
        NodeRepository nodeRepository = NodeRepository.getInstance();
        nodeRepository.setAgentContainer(rt.createAgentContainer(profile));

        // Start SyncAgent on system launch
        try {
            AgentController syncAgent = nodeRepository.getAgentContainer().createNewAgent(
                    "SyncAgent",
                    "kaasenwijn.namenode.agents.SyncAgent",
                    null
            );
            syncAgent.start();
            System.out.println("[Startup] SyncAgent launched and running infinitely.");
        } catch (Exception e) {
            System.err.println("[Startup] Failed to launch SyncAgent");
            e.printStackTrace();
        }

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
        Thread.sleep(rand.nextInt(500));
        NodeSender.sendMulticastMessage("bootstrap");
        Thread.sleep(rand.nextInt(500));
        NodeSender.sendMulticastMessage("bootstrap");
        Thread.sleep(rand.nextInt(500));
        NodeSender.sendMulticastMessage("bootstrap");


        // Register a Shutdown hook
        // https://www.baeldung.com/jvm-shutdown-hooks

        Thread shutdownHook = new Thread(NodeService::shutdown);
        java.lang.Runtime.getRuntime().addShutdownHook(shutdownHook);

        // Start periodic health-check
        // Create a scheduled executor with one thread
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Define the task to run
        Runnable task = () -> {
            System.out.println("Health-check running at " + java.time.LocalTime.now());
            new Failure().healthCheck();
        };

        // Schedule the task to run every 20 seconds with no initial delay
        scheduler.scheduleAtFixedRate(task, 0, 100, TimeUnit.SECONDS);

        FileMonitor.getInstance().start();
    }

}
