package kaasenwijn.NetworkManager.model;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
public class NodeInfo {

    protected static class Port{
        private int hostPort;
        private int port;

        public Port(int hostPort, int port) {
            this.hostPort = hostPort;
            this.port = port;
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
    }
   public static class Info{
        private int currentId;
        private int previousId;
        private int nextId;
        private String name;

        private Boolean status;

       public Boolean getStatus() {
           return status;
       }

       public void setStatus(Boolean status) {
           this.status = status;
       }

       public Info(int currentId, int previousId, int nextId, String name) {
            this.currentId = currentId;
            this.previousId = previousId;
            this.nextId = nextId;
            this.name = name;
        }

       public Info(int currentId, int previousId, int nextId, String name, Boolean status) {
           this.currentId = currentId;
           this.previousId = previousId;
           this.nextId = nextId;
           this.name = name;
           this.status = status;
       }

       public int getCurrentId() {
            return currentId;
        }

        public void setCurrentId(int currentId) {
            this.currentId = currentId;
        }

        public int getPreviousId() {
            return previousId;
        }

        public void setPreviousId(int previousId) {
            this.previousId = previousId;
        }

        public int getNextId() {
            return nextId;
        }

        public void setNextId(int nextId) {
            this.nextId = nextId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    private  Info info;
    private Port port;
    private List<String> localFiles;
    private List<String> replicatedFiles;

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public NodeInfo(Info info, List<String> localFiles, List<String> replicatedFiles) {
        this.info = info;
        this.localFiles = localFiles;
        this.replicatedFiles = replicatedFiles;
    }

    public List<String> getLocalFiles() {
        return localFiles;
    }

    public void setLocalFiles(List<String> localFiles) {
        this.localFiles = localFiles;
    }

    public List<String> getReplicatedFiles() {
        return replicatedFiles;
    }

    public void setReplicatedFiles(List<String> replicatedFiles) {
        this.replicatedFiles = replicatedFiles;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public static NodeInfo fromJSONObject(JSONObject json) {
        JSONObject info = json.getJSONObject("info");
        int currentId = info.getInt("currentId");
        int previousId = info.optInt("previousId", -1);
        int nextId = info.optInt("nextId", -1);
        String name = info.optString("name", "");

        List<String> localFiles = new ArrayList<>();
        JSONArray localFilesArray = json.optJSONArray("localFiles");
        if (localFilesArray != null) {
            for (int i = 0; i < localFilesArray.length(); i++) {
                localFiles.add(localFilesArray.getString(i));
            }
        }

        List<String> replicatedFiles = new ArrayList<>();
        JSONArray replicatedFilesArray = json.optJSONArray("replicatedFiles");
        if (replicatedFilesArray != null) {
            for (int i = 0; i < replicatedFilesArray.length(); i++) {
                replicatedFiles.add(replicatedFilesArray.getString(i));
            }
        }

        return new NodeInfo(new Info(currentId, previousId, nextId, name), localFiles, replicatedFiles);
    }

    public static List<NodeInfo> fromJSONArray(JSONArray array) {
        List<NodeInfo> nodes = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            NodeInfo node = fromJSONObject(obj); // Reuse your existing method
            nodes.add(node);
        }

        return nodes;
    }

    public void addPortInfo(Node node){
        Port port = new Port(node.getHostPort(), node.getPort());
        setPort(port);
    }


}
