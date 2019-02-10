package Nuskin;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends CrudRepository<Product, Integer> {
	
    ArrayList<Product> findAllByOrderOrderNumber(@Param("order_order_number") String orderNumber);


}
