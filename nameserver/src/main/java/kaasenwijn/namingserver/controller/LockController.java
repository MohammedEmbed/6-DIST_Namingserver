package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.model.FileLock;
import kaasenwijn.namingserver.service.LockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lock")
public class LockController {

    private final LockService lockService;

    public LockController(LockService lockService) {
        this.lockService = lockService;
    }

    @PostMapping("/acquire")
    public ResponseEntity<String> acquireLock(@RequestBody FileLock lockRequest) {
        boolean acquired = lockService.acquireLock(lockRequest);
        return acquired ?
                ResponseEntity.ok("Lock acquired") :
                ResponseEntity.status(HttpStatus.CONFLICT).body("Lock already held by another node");
    }

    @DeleteMapping("/release")
    public ResponseEntity<String> releaseLock(@RequestBody FileLock lockRequest) {
        boolean released = lockService.releaseLock(lockRequest);
        return released ?
                ResponseEntity.ok("Lock released") :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lock was not held by this node");
    }

    @PostMapping("/renew")
    public ResponseEntity<String> renewLock(@RequestBody FileLock lockRequest) {
        boolean renewed = lockService.renewLock(lockRequest);
        return renewed ?
                ResponseEntity.ok("Lock renewed") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lock not found");
    }

    @DeleteMapping("/release_by_node/{nodeId}")
    public ResponseEntity<String> releaseAllLocksByNode(@PathVariable int nodeId) {
        lockService.releaseAllLocksByNode(nodeId);
        return ResponseEntity.ok("All locks released for node " + nodeId);
    }

}
