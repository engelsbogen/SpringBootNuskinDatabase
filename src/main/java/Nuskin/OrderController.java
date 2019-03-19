package Nuskin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class OrderController {

	OrderRepository orderRepo;
	
	OrderController(OrderRepository repo) {
		this.orderRepo = repo;
	}
	
	@RequestMapping(value="/orders")
	public Iterable<Order> getOrders() {
	    
		Iterable<Order> orders = orderRepo.findAll();
		
		// Products on the order are marked transient, not loaded from the database
		// Need to get them now so that we can work out if any items still dont have an end user
		for (Order order: orders) {
			order.getProductsFromDatabase();
		}
		
		//List<Order> target = new ArrayList<>();
		//orders.forEach(target::add);  // :: syntax is a method reference
		//orders.forEach( n-> {target.add(n); } );
		return orders;
    }
	
	
	
	@RequestMapping("/orderfiledownload")
	public  ResponseEntity<Resource> orderFileDownload(@RequestParam("orderNumber") String orderNumber, Model model) {

		String filename = orderNumber + ".pdf";
		Path path = Paths.get(FileRoot.getRoot() + "Orders/" + filename);
		File file = path.toFile();

		HttpHeaders headers = new HttpHeaders();
	    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
		
		ByteArrayResource resource = null;
		try {
			resource = new ByteArrayResource(Files.readAllBytes(path));
			return ResponseEntity.ok()
		            .headers(headers)
		            .contentLength(file.length())
		            .contentType(MediaType.parseMediaType("application/octet-stream"))
		            .body(resource);
		} catch (IOException e) {
			
			model.addAttribute("orderfilename", filename);
			
			throw new ResponseStatusException(
					  HttpStatus.NOT_FOUND, "Order file \"" + filename + "\"" 
					);
		}
	}
	
	
	// POST 
    @PostMapping("/orderfileupload")
    public ArrayList<Product> orderFileUpload(@RequestParam("file") MultipartFile file) {
    	
    	
    	ArrayList<Product> unknownProductTypes = new ArrayList<Product>();
    	
    	System.out.println("Uploading file " + file.getOriginalFilename());

    	Order order = null;
    	
    	// Parse PDF
    	if (file.getContentType().equals("application/pdf")) {
    		
    		try {
			    order = new OrderParserPDF().parse(file.getInputStream());
				
				File dest = new File(FileRoot.getRoot() + "/Orders/" + order.getOrderNumber() + ".pdf");
				
				file.transferTo(dest);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}

    	if (order != null) {

    		if (!order.hasAllInfo())
    		{
    		    // Something went wrong with the parsing.
    			throw new ResponseStatusException(
  					  HttpStatus.UNPROCESSABLE_ENTITY, file.getOriginalFilename() + " is corrupt or incomplete" 
  					);

    		}

    		else {
        		unknownProductTypes = order.getUnknownProductTypes();
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
