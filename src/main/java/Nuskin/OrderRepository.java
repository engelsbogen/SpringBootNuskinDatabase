package Nuskin;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {

	@Transactional
	void deleteById(String id);
    
}