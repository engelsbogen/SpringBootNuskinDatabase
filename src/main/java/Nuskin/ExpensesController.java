package Nuskin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExpensesController {

	@Autowired
	ExpensesRepository expensesRepo;
	
    @PutMapping("/addexpense")
    public DummyResponse  addExpense(@RequestBody Expense newExpense) {
    	
		expensesRepo.save(newExpense);
    	
		// Flush the changes  
		expensesRepo.flush();
		
        return new DummyResponse();
    }

    
   


    @DeleteMapping(value="/deleteexpense")
    public ResponseEntity<?>  deleteExpense(@RequestParam Long id) {
    	
    	if (expensesRepo.existsById(id)) {
    	
    		expensesRepo.deleteById(id);
    	
    		//	Flush the changes  
    		expensesRepo.flush();

    	    HttpHeaders headers = new HttpHeaders();
    	    headers.add("Content-Type", "application/json; charset=UTF-8");

   		
        	return ResponseEntity.ok().headers(headers).build();
    	}
    	else {
    		return ResponseEntity.badRequest().build();
    	}
    }

    
    
	@RequestMapping(value="/expenses")
	public ExpenseReport getReport(@RequestParam String period) {
		
		// Period is for example "year", "Jan", maybe extend later to month range "Jan-Apr" or "1st Quarter" etc
		ExpenseReport report = new ExpenseReport().build(period);
		return report;
		
	}

    
	
}
