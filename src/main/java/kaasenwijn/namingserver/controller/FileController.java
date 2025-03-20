package kaasenwijn.namingserver.controller;

import kaasenwijn.namingserver.repository.IpRepository;
import kaasenwijn.namingserver.service.NameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/file")
@RestController
public class FileController {
    @Autowired
    private NameService nameService;

    private final IpRepository ipRepo = IpRepository.getInstance();

    //TODO:  add file functions...
    @GetMapping("/{filePath}")
    public synchronized String getFileLocation(@PathVariable String filePath) {
        Integer id = nameService.getNodeId(filePath);
        return ipRepo.getIp(id);
    }
}
