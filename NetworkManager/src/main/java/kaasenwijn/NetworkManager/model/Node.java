package kaasenwijn.NetworkManager.model;

public class Node {
    private int hostPort;
    private int port;
    private String name;

    public Node(int hostPort, int port, String name) {
        this.hostPort = hostPort;
        this.port = port;
        this.name = name;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Node with properties: " +
                "hostPort=" + hostPort +
                ", port=" + port +
                ", name='" + name + '\'';
    }
}
