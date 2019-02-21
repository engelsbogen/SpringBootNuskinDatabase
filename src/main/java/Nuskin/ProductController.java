package Nuskin;

import java.util.ArrayList;

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
	    
}
