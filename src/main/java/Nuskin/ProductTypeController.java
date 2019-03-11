package Nuskin;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductTypeController {

	
	ProductTypeRepository productTypeRepo;
	
	ProductTypeController(ProductTypeRepository repo) {
		this.productTypeRepo = repo;
	}
	
	
    @PutMapping("/createskus")
    public ResponseEntity<?>  createSkus(@RequestBody ArrayList<ProductType> newSkus) {
    	
    	
    	// We only want to update EndUse, Customer, Selling Price and Receipt number
    	
    	// So - fetch the current, overwrite those fields, write it back?
    	for (ProductType newSku : newSkus) {
    		
    			
   			productTypeRepo.save(newSku);

    	}
    	
		// Flush the changes  
		productTypeRepo.flush();
		
		System.out.println("Flushed database changes");
		
        return ResponseEntity.ok().build();
    }
}
