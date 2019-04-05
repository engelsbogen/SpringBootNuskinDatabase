package Nuskin;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

	ProductRepository productRepo;
	
	ProductController(ProductRepository repo) {
		this.productRepo = repo;
	}
	
	@RequestMapping(value="/products")
	public ArrayList<Product> getProducts(@RequestParam String orderNumber) {
	    
		ArrayList<Product> products = productRepo.findAllByOrderOrderNumber(orderNumber);
		return products;
    }
	
	class Dummy {
		int id = 0;

		public int getId() {
			return id;
		}
	}
	
    @PutMapping("/updateitems")
    public Dummy updateItems(@RequestBody ArrayList<Product> updatedItems) {
    	
    	// We only want to update EndUse, Customer, Selling Price and Receipt number
    	
    	// So - fetch the current, overwrite those fields, write it back?
    	for (Product updatedItem : updatedItems) {
    		
    		Optional<Product> existingItemOpt = productRepo.findById(updatedItem.getId());
    		
    		if (existingItemOpt.isPresent()) {
    			
    			Product existingItem = existingItemOpt.get();
    			existingItem.setReceiptNumber(updatedItem.getReceiptNumber());
    			existingItem.setEndUse(updatedItem.getEndUse());
    			existingItem.setSellingPrice(updatedItem.getSellingPrice().toString());
    			existingItem.setCustomerName(updatedItem.getCustomerName());
    			
    			// This actually creates or updates - but as I know its an existing item it just updates
    			productRepo.save(existingItem);
    			
    		}

    	}
    	
		// Flush the changes  
		productRepo.flush();
		
		// Previously just had this returning ResponseEntity.ok(), however that has no content and no Content-Type header,
		// which gets interpreted as an XML response in Firefox, and I get an error message in the console window.
		// It seems this is a Firefox feature, Chrome is OK (googling confirms)
		// Everything works fine apart from the error message, but if I return an object here, it becomes a json
		// content type and there are no errors in the console.
        return new Dummy();
    }

	    
}
