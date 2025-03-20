package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.service.NameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/node")
@RestController
public class NodeController {
    @Autowired
    private NameService nameService;

    // TODO: create node crud

}
