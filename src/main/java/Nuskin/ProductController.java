package Nuskin;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

	ProductRepository productRepo;
	
	ProductController(ProductRepository repo) {
		this.productRepo = repo;
	}
	
	@RequestMapping(value="/products")
	public ArrayList<Product> getProducts() {
	    
		ArrayList<Product> products = productRepo.findAllByOrderOrderNumber("0235024261");
		//List<Order> target = new ArrayList<>();
		//orders.forEach(target::add);  // :: syntax is a method reference
		//orders.forEach( n-> {target.add(n); } );
		return products;
    }
	    
}
