package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.model.ErrorDto;
import kaasenwijn.namingserver.model.Neighbours;
import kaasenwijn.namingserver.model.Node;
import kaasenwijn.namingserver.service.NameService;
import kaasenwijn.namingserver.repository.IpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
        Integer hash = nameService.getHash(n.name);
        if(!ipRepo.ipExists(hash)){
            ipRepo.setIp(hash, n.ip);
        } else{
            return ResponseEntity.badRequest().body(new ErrorDto("Node already exists"));
        }
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("ip/{hash}")
    public ResponseEntity<?> GetIPByHash(@PathVariable int hash) {
        System.out.println("Ip of "+hash+" requested: ");
        if(!ipRepo.ipExists(hash)){
            System.out.println("Node with hash: '"+hash+"' not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        return ResponseEntity.ok().body(new Node(null, ipRepo.getIp(hash)));
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> GetNode(@PathVariable String name) {
        Integer hash = nameService.getHash(name);
        System.out.println("Hash "+name+" requested: "+hash.toString());
        if(!ipRepo.ipExists(hash)){
            System.out.println("Node "+name+" not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        return ResponseEntity.ok().body(new Node(name,ipRepo.getIp(hash)));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> Delete(@PathVariable String name) {
        Integer hash = nameService.getHash(name);
        if(!ipRepo.ipExists(hash)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        ipRepo.remove(hash);
        System.out.println("Node "+name+" deleted");
        return ResponseEntity.ok().body(null);
    }


    @GetMapping("/nb/{id}")
    public ResponseEntity<?> GetNodeIds(@PathVariable int id) {
        if(!ipRepo.ipExists(id)){
            System.out.println("Node "+id+" not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        Neighbours data = NameService.getNeighbours(id);
        return ResponseEntity.ok().body(data);
    }

}
