package kaasenwijn.namenode.model;

import jade.wrapper.AgentContainer;

public class NodeStructure {

    public int currentId;
    public String selfIp;

    public int selfPort;

    public Neighbor previous;
    public Neighbor next;

    public String namingServerIp;

    public int namingServerPort;

    public int namingServerHTTPPort;

    public String name;

    public AgentContainer agentContainer;



}
