package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.model.*;
import kaasenwijn.namingserver.repository.IpRepository;
import kaasenwijn.namingserver.service.NameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private NameService nameService;
    private final IpRepository ipRepo = IpRepository.getInstance();

    @GetMapping("/location/{nodeHash}/{fileHash}")
    public ResponseEntity<?> getFileReplicationLocation(@PathVariable int nodeHash, @PathVariable int fileHash) {
        int id = NameService.getFileOwnerId(fileHash, nodeHash);

        if (!ipRepo.ipExists(id)) {
            System.out.println("Node " + id + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Node doesn't exists"));
        }
        String ip = ipRepo.getIp(id);
        return ResponseEntity.ok().body(new NodeIp(nodeHash,ip));
    }
}
