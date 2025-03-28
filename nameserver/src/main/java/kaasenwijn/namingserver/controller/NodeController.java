package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.model.ErrorDto;
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

    @GetMapping("/{name}")
    public ResponseEntity<?> GetNode(@PathVariable String name) {
        Integer hash = nameService.getHash(name);
        if(!ipRepo.ipExists(hash)){
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
        return ResponseEntity.ok().body(null);
    }

}
