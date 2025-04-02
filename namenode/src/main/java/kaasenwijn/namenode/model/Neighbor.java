package kaasenwijn.namenode.model;


public class Neighbor {
    public Integer Id;
    public String Ip;

    /**
     * @param Id hash of the neighbouring node
     * @param Ip ip of the neighbouring node
     */
    public Neighbor(Integer Id, String Ip) {
        this.Id = Id;
        this.Ip = Ip; // TODO: is this IP needed?
    }
}
