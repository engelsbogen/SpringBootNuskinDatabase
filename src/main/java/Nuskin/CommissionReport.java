package Nuskin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class CommissionReport {
	
	
	String period;
	List<Commission> commission = new ArrayList<Commission>();
	
	public String getPeriod() {
		return period;
	}

	public List<Commission> getCommission() {
		return commission;
	}
	
	
	public BigDecimal getTotal() {
		
		BigDecimal total = BigDecimal.ZERO;
		for (Commission c: commission) {
			
			total = total.add(c.getAmount());
		}
		return total;
	}
	

	CommissionReport build(String period) {
		
		ProductDatabase db = ProductDatabase.getDB();
		
		this.period = Util.periodToLongName(period);
		this.commission = db.findAllCommission();
		
		if ( !period.equals("year")) {
			// Filter to keep only those for the requested month
			
			int monthValue = Util.periodToMonthValue(period);
			
			List<Commission> monthCommission = this.commission;
			
			this.commission = monthCommission.stream()
     	                   .filter( p -> p.getDate().getMonthValue() == monthValue )
       			           .collect(Collectors.toList());
			
		}
		return this;
	}
}
