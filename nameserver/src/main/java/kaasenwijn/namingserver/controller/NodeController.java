package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.model.ErrorDto;
import kaasenwijn.namingserver.model.Neighbours;
import kaasenwijn.namingserver.model.Node;
import kaasenwijn.namingserver.model.NodeIp;
import kaasenwijn.namingserver.service.NameService;
import kaasenwijn.namingserver.repository.IpRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/node")
public class NodeController {
    @Autowired
    private NameService nameService;
    private final IpRepository ipRepo = IpRepository.getInstance();

    @PostMapping
    public ResponseEntity<?> Create(@RequestBody Node n) {
        Integer hash = NameService.getHash(n.name);
        if (!ipRepo.ipExists(hash)) {
            ipRepo.setIp(hash, n.ip);
        } else {
            return ResponseEntity.badRequest().body(new ErrorDto("Node already exists"));
        }
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("ip/{hash}")
    public ResponseEntity<?> GetIPByHash(@PathVariable int hash) {
        System.out.println("Ip of " + hash + " requested: ");
        if (!ipRepo.ipExists(hash)) {
            System.out.println("Node with hash: '" + hash + "' not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        return ResponseEntity.ok().body(new Node(null, ipRepo.getIp(hash)));
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> GetNode(@PathVariable String name) {
        Integer hash = NameService.getHash(name);
        System.out.println("Hash " + name + " requested: " + hash.toString());
        if (!ipRepo.ipExists(hash)) {
            System.out.println("Node " + name + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        return ResponseEntity.ok().body(new Node(name, ipRepo.getIp(hash)));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> Delete(@PathVariable String name) {
        Integer hash = NameService.getHash(name);
        if (!ipRepo.ipExists(hash)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        ipRepo.remove(hash);
        System.out.println("Node " + name + " deleted");
        return ResponseEntity.ok().body(null);
    }

    @DeleteMapping("hash/{hash}")
    public ResponseEntity<?> DeleteByHash(@PathVariable int hash) {
        if (!ipRepo.ipExists(hash)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        ipRepo.remove(hash);
        System.out.println("Nodehash " + hash + " deleted");
        return ResponseEntity.ok().body(null);
    }


    @GetMapping("/nb/{id}")
    public ResponseEntity<?> GetNodeIds(@PathVariable int id) {
        if (!ipRepo.ipExists(id)) {
            System.out.println("Node " + id + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        Neighbours data = NameService.getNeighbours(id);
        return ResponseEntity.ok().body(data);
    }

    // Get the information of a specific node
    @GetMapping("/info/{name}")
    public ResponseEntity<?> GetNodeInfo(@PathVariable String name) {
        int id = NameService.getHash(name);
        if (!ipRepo.ipExists(id)) {
            System.out.println("Node " + id + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        NodeIp node = new NodeIp(id, ipRepo.getIp(id));
        int port = node.port+1;
        String dest = node.ip+":"+port;
        JSONObject data = NameService.sendServerGetRequest(dest,"/api/node/info");
        assert data != null;
        return ResponseEntity.ok().body(data.toString());
    }

    // Get all nodes in the network and their information
    @GetMapping("/info/all")
    public ResponseEntity<?> GetNodeInfoAll() {
        JSONArray dataList = new JSONArray();
        for(String ip: ipRepo.getMap().values()){
            NodeIp node = new NodeIp(0, ip);
            int port = node.port+1;
            String dest = node.ip+":"+port;
            JSONObject data = NameService.sendServerGetRequest(dest,"/api/node/info");
            if(data != null){
                dataList.put(data);
            }
        }
        return ResponseEntity.ok().body(dataList.toString());
    }

}
