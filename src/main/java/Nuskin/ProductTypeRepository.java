package Nuskin;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ProductTypeRepository extends JpaRepository<ProductType, String> {

    Optional<ProductType> findBySku(@Param("sku") String sku);
    
}