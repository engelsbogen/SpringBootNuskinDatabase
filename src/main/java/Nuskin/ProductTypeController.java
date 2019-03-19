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
    	
		productTypeRepo.saveAll(newSkus);
    	
		// Flush the changes  
		productTypeRepo.flush();
		
        return ResponseEntity.ok().build();
    }
}
