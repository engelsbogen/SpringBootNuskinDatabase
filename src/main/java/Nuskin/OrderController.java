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
		//List<Order> target = new ArrayList<>();
		//orders.forEach(target::add);  // :: syntax is a method reference
		//orders.forEach( n-> {target.add(n); } );
		return orders;
    }
	    
}
