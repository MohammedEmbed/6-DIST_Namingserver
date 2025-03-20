package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.service.NameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/file")
@RestController
public class FileController {
    @Autowired
    private NameService nameService;

    //TODO:  add file functions...

}
