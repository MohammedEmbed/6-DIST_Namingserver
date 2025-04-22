package kaasenwijn.namingserver.model;

public class Neighbours {
    public NodeIp next;
    public NodeIp previous;

    public Neighbours(NodeIp next, NodeIp previous) {
        this.next = next;
        this.previous = previous;
    }
}
