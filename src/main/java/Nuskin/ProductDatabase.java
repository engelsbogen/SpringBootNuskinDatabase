package Nuskin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductDatabase {

	private static ProductDatabase theProductDatabase;
	
	@Autowired
    private  ProductRepository productRepository;
	@Autowired 
    private  ProductTypeRepository productTypeRepository;
	@Autowired 
    private  OrderRepository orderRepository;

	public static ProductDatabase getDB() { return theProductDatabase; }
	
	
	ProductDatabase() {
		// Bodge to make singleton 
		theProductDatabase = this;
	}
	

	void addProductType(ProductType productType) {
		productTypeRepository.save(productType);
	}
	
	void addProduct(Product product) {
		productRepository.save(product);
	}
	
	void addOrder(Order order) {
		orderRepository.save(order);
	}
	
	ProductType findProductType(String SKU) {
		Optional<ProductType> productType = productTypeRepository.findBySku(SKU);
		
		if (!productType.isPresent()) {
			System.err.println("Product " + SKU + " not found in database");
		}
		
		return productType.get();
	}
	
}
