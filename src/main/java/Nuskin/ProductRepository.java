package Nuskin;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

// JPA repository rather than CrudRepository gives me flush operations 
public interface ProductRepository extends JpaRepository<Product, Integer> {
	
    ArrayList<Product> findAllByOrderOrderNumber(@Param("order_order_number") String orderNumber);

    Optional<Product> findById(@Param("id") Long id);


}
