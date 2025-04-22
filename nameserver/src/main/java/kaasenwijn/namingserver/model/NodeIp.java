package kaasenwijn.namingserver.model;

public class NodeIp {
    public String ip;

    public int port;
    public int id;

    public NodeIp(int id,String ipWithPort) {
        this.id = id;
        String[] parts = ipWithPort.split(":");
        this.ip = parts[0];
        this.port = Integer.parseInt(parts[1]);
    }
}
