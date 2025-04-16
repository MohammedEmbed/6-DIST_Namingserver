package kaasenwijn.namenode.model;
import kaasenwijn.namenode.model.Neighbor;

public class NodeStructure {

    public int currentId;
    public String selfIp;

    public int selfPort;

    public Neighbor previous;
    public Neighbor next;

    public String namingServerIp;

    public String name;


}
