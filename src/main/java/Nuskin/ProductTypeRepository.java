package Nuskin;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProductTypeRepository extends CrudRepository<ProductType, String> {

    Optional<ProductType> findBySku(@Param("sku") String sku);
    
}