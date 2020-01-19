package Nuskin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BackupController {

    
    @PostMapping("/backup")
    public DummyResponse  backup() {
        
        boolean status = ProductDatabase.getDB().backup();
        
        DummyResponse resp = new DummyResponse();
        
        resp.setId(status ? 0 : 1);
        return resp;
    }

}