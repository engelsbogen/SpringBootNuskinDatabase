package Nuskin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	    
}
