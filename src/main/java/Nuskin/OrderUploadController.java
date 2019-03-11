package Nuskin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class OrderUploadController {

	// POST 
    @PostMapping("/orderfileupload")
    public ArrayList<Product> orderFileUpload(@RequestParam("file") MultipartFile file) {
    	
    	
    	ArrayList<Product> unknownProductTypes = new ArrayList<Product>();
    	
    	System.out.println("Uploading file " + file.getOriginalFilename());

    	// Parse PDF
    	if (file.getContentType().equals("application/pdf")) {
    		
    		try {
				Order order = new OrderParserPDF().parse(file.getInputStream());
	
				
				File dest = new File(FileRoot.getRoot() + "/Orders/" + order.getOrderNumber() + ".pdf");
				
				file.transferTo(dest);
				
				unknownProductTypes = order.getUnknownProductTypes();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}

    	
    	//https://www.nuskin.com/content/nuskin/en_CA/cpm/order-details.html
    		
	    //} catch (IOException ioe) {
	    //    //if something went wrong, we need to inform client about it
	    //    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    //}

        //return ResponseEntity.ok().build();
    	return unknownProductTypes;
    }

}
