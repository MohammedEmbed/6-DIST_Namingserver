package kaasenwijn.namenode.util;

// Custom checked exception
public class CommunicationException extends Exception {
    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public CommunicationException() {
        super();
    }

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommunicationException(Throwable cause) {
        super(cause);
    }

    public CommunicationException(String ip, int port) {
        super("Communication to Node with IP: "+ip+" and PORT: "+port+" failed.");
        this.ip = ip;
        this.port = port;
    }
}
