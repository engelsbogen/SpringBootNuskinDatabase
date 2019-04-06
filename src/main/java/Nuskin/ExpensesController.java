package Nuskin;

import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?>  addExpense(@RequestBody Expense newExpense) {
    	
		expensesRepo.save(newExpense);
    	
		// Flush the changes  
		expensesRepo.flush();
		
        return ResponseEntity.ok().build();
    }

    
	int periodToMonthValue(String period) {
		
        return Month.from(DateTimeFormatter.ofPattern("MMM").parse(period)).getValue();
	}
    
    class ExpenseReport {
    	
    	String period;
		List<Expense> expenses = new ArrayList<Expense>();
    	
    	public String getPeriod() {
			return period;
		}

		public List<Expense> getExpenses() {
			return expenses;
		}

    	void build(String period) {
    		
    		this.period = period;
    		this.expenses = expensesRepo.findAll();
    		
    		if ( !period.equals("year")) {
    			// Filter to keep only those for the requested month
    			
    			int monthValue = periodToMonthValue(period);
    			
    			List<Expense> monthExpenses = this.expenses;
    			
    			this.expenses = monthExpenses.stream()
         	                   .filter( p -> p.getDate().getMonthValue() == monthValue )
           			           .collect(Collectors.toList());
    			
    		}
    	}
    }

    @DeleteMapping(value="/deleteexpense")
    public ResponseEntity<?>  deleteExpense(@RequestParam Long id) {
    	
    	if (expensesRepo.existsById(id)) {
    	
    		expensesRepo.deleteById(id);
    	
    		//	Flush the changes  
    		expensesRepo.flush();
		
        	return ResponseEntity.ok().build();
    	}
    	else {
    		return ResponseEntity.badRequest().build();
    	}
    }

    
    
	@RequestMapping(value="/expenses")
	public ExpenseReport getReport(@RequestParam String period) {
		
		ExpenseReport report = new ExpenseReport();

		// Period is for example "year", "January", "Jan-Apr", "month"
		report.build(period);
		
		return report;
		
	}

    
	
}
