package Nuskin;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface ExpensesRepository extends JpaRepository<Expense, String> {

    @Transactional
	void deleteById(Long id);

	boolean existsById(Long id);
    
}
