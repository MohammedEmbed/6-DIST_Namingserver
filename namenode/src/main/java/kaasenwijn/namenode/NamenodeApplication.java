package kaasenwijn.namenode;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import kaasenwijn.namenode.service.FileMonitor;
import kaasenwijn.namenode.service.NodeService;
import kaasenwijn.namenode.util.Failure;
import kaasenwijn.namenode.util.NodeMulticastReceiver;
import kaasenwijn.namenode.util.NodeSender;
import kaasenwijn.namenode.util.NodeUnicastReceiver;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jade.core.*;
import jade.core.Runtime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class NamenodeApplication {

    public static void main(String[] args) throws InterruptedException{
        String ip = System.getenv("SERVER_IP");
        int port = Integer.parseInt(System.getenv("SERVER_PORT"));
        String hostName = System.getenv("SERVER_NAME");
        System.out.println("Node started with IP-address: " + ip + ", Port: " + port + " and Name: " + hostName);

        NodeService.startUp(ip, port, hostName);

        // Initialize JADE container
        Runtime rt = jade.core.Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter("main", "false");
        profile.setParameter("containerName", hostName);
        AgentContainer container = rt.createAgentContainer(profile);

        // Start SyncAgent on system launch
        try {
            AgentController syncAgent = container.createNewAgent(
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
