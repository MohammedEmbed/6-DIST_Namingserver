package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.model.Node;
import kaasenwijn.namingserver.service.NameService;
import kaasenwijn.namingserver.repository.IpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/node")
public class NodeController {
    @Autowired
    private NameService nameService;
    private final IpRepository ipRepo = IpRepository.getInstance();

    @PostMapping
    public void Update(@RequestBody Node n) {
        Integer hash = nameService.getHash(n.name);
        ipRepo.setIp(hash, n.ip);
    }

    @GetMapping("/{name}")
    public Node GetNode(@PathVariable String name) {
        Integer hash = nameService.getHash(name);
        return new Node(name,ipRepo.getIp(hash));
    }

    @DeleteMapping("/{name}")
    public void Delete(@PathVariable String name) {
        Integer hash = nameService.getHash(name);
        ipRepo.remove(hash);
    }

}
