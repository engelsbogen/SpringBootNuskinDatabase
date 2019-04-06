package Nuskin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ExpenseReport {
	
	String period;
	List<Expense> expenses = new ArrayList<Expense>();
	
	public String getPeriod() {
		return period;
	}

	public List<Expense> getExpenses() {
		return expenses;
	}
	
	public BigDecimal getTotal() {
		
		BigDecimal total = BigDecimal.ZERO;
		for (Expense e: expenses) {
			
			total = total.add(e.getAmount());
		}
		return total;
	}
	

	ExpenseReport build(String period) {
		
		ProductDatabase db = ProductDatabase.getDB();
		
		this.period = Util.periodToLongName(period);
		this.expenses = db.findAllExpenses();
		
		if ( !period.equals("year")) {
			// Filter to keep only those for the requested month
			
			int monthValue = Util.periodToMonthValue(period);
			
			List<Expense> monthExpenses = this.expenses;
			
			this.expenses = monthExpenses.stream()
     	                   .filter( p -> p.getDate().getMonthValue() == monthValue )
       			           .collect(Collectors.toList());
			
		}
		
		return this;
	}
}