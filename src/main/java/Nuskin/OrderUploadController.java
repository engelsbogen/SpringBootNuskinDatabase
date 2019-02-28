package Nuskin;

import java.io.File;
import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class OrderUploadController {

	// POST 
    @PostMapping("/orderfileupload")
    public ResponseEntity<?> orderFileUpload(@RequestParam("file") MultipartFile file) {
    	
    	System.out.println("Uploading file " + file.getOriginalFilename());

    	// Parse PDF
    	if (file.getContentType().equals("application/pdf")) {
    		
    		try {
				new OrderParserPDF().parse(file.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    	
	    //} catch (IOException ioe) {
	    //    //if something went wrong, we need to inform client about it
	    //    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    //}

        return ResponseEntity.ok().build();
    }

}
