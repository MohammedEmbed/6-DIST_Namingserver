package kaasenwijn.namenode.controller;

import kaasenwijn.namenode.service.NodeService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/node")
public class NodeController {

    @GetMapping("/info")
    public ResponseEntity<?> GetInfo() {
        JSONObject data = NodeService.getNodeInfo();
        return ResponseEntity.ok().body(data.toString());
    }

    @GetMapping("/status")
    public ResponseEntity<Void> getStatus() {
        return ResponseEntity.ok().build();
    }

}
