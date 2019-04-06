package Nuskin;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface ExpensesRepository extends JpaRepository<Expense, Long> {

    @Transactional
	void deleteById(Long id);
    
}
