package kaasenwijn.node.model;


public class Neighbour {
    public Integer Id;
    public String Ip;

    /**
     * @param Id hash of the neighbouring node
     * @param Ip ip of the neighbouring node
     */
    public Neighbour(Integer Id, String Ip) {
        this.Id = Id;
        this.Ip = Ip;
    }
}
