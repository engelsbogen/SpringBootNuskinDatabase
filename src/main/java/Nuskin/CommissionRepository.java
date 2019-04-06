package Nuskin;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionRepository extends JpaRepository<Commission, Long> {

    @Transactional
	void deleteById(Long id);

}