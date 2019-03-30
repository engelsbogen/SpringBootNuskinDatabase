package Nuskin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class Report {

	BigDecimal openingInventory = BigDecimal.ZERO;
	BigDecimal purchases = BigDecimal.ZERO;
	BigDecimal grossSales = BigDecimal.ZERO;          // Selling price of sold items
    BigDecimal closingInventory = BigDecimal.ZERO;
    BigDecimal personalUseCost = BigDecimal.ZERO;


    BigDecimal salesCost = BigDecimal.ZERO;   // Cost price of sold items
	BigDecimal sampleCost = BigDecimal.ZERO;
    BigDecimal demoCost = BigDecimal.ZERO;
    BigDecimal itemCost = BigDecimal.ZERO;
    BigDecimal itemTax = BigDecimal.ZERO;
    BigDecimal itemShipping = BigDecimal.ZERO;
    
	Integer openingInventoryItemCount = 0;
    Integer purchasedItemCount = 0;
    Integer orderCount = 0;
    Integer soldItemCount = 0;
    Integer sampleItemCount = 0;
    Integer personalUseItemCount = 0;
    Integer inventoryItemCount = 0;
    Integer demoItemCount = 0;
    
    String period = null;
 
    public String getPeriod() {
		return period;
	}

	// Profit/(loss) is grossSales - (purchases + openingInventory - closingInventory)
	public BigDecimal getOpeningInventory() {
		return openingInventory;
	}

	public Integer getOpeningInventoryItemCount() {
		return openingInventoryItemCount;
	}

	public BigDecimal getPurchases() {
		return purchases;
	}

	public BigDecimal getGrossSales() {
		return grossSales;
	}

	public BigDecimal getClosingInventory() {
		return closingInventory;
	}

	public Integer getPurchasedItemCount() {
		return purchasedItemCount;
	}

	public Integer getOrderCount() {
		return orderCount;
	}

	public Integer getSoldItemCount() {
		return soldItemCount;
	}

	public Integer getSampleItemCount() {
		return sampleItemCount;
	}

	public Integer getPersonalUseItemCount() {
		return personalUseItemCount;
	}

	public Integer getInventoryItemCount() {
		return inventoryItemCount;
	}

	public Integer getDemoItemCount() {
		return demoItemCount;
	}

	public BigDecimal getItemCost() {
		return itemCost;
	}

	public BigDecimal getItemTax() {
		return itemTax;
	}

	public BigDecimal getItemShipping() {
		return itemShipping;
	}
    public BigDecimal getPersonalUseCost() {
		return personalUseCost;
	}

	public BigDecimal getSampleCost() {
		return sampleCost;
	}
	
	public BigDecimal getSalesCost() {
		return salesCost;
	}

	public BigDecimal getDemoCost() {
		return demoCost;
	}

	int periodToInt(String period) {
		
		final String[] months = {"year", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		
		for (int i=0; i < months.length; i++) {
			
			if (period.equals(months[i])) {
				return i; 
			}
			
		}
		return 0;
	}
	
	
	String periodToLongName(String period) {
		
	    Map<String, String> map = new HashMap<String, String>();
	    map.put("year", "year to date");
	    map.put("Jan", "January");
	    map.put("Feb", "February");
	    map.put("Mar", "March");
	    map.put("Apr", "April");
	    map.put("May", "May");
	    map.put("Jun", "June");
	    map.put("Jul", "July");
	    map.put("Aug", "August");
	    map.put("Sep", "September");
	    map.put("Oct", "October");
	    map.put("Nov", "November");
	    map.put("Dec", "December");

	    return map.get(period);
	    
	}
	
	void build(String period) {
		
		// Firstly just build report for the entire year
		
		System.out.println("Generating report for " + period);
		
		ProductDatabase db = ProductDatabase.getDB();
		
		Iterable<Order> orders = db.getOrders();
		
		this.period = periodToLongName(period);
		
	
		for (Order order : orders) {
			
			if (period.equals("year") || period.equals(order.getMonthName())) {
				
				order.correctTaxAndShipping();
				
				purchases = purchases.add(order.getTotal());
				
				grossSales = grossSales.add(order.getSales());
				
				closingInventory = closingInventory.add(order.getInventoryCost());
	
				salesCost = salesCost.add(order.getSalesCost());
				personalUseCost = personalUseCost.add(order.getPersonalUseCost());
				sampleCost = sampleCost.add(order.getSampleCost());
				demoCost = demoCost.add(order.getDemoCost());
				
				itemCost = itemCost.add(order.getItemCost());
				itemTax = itemTax.add(order.getItemTax());
				itemShipping = itemShipping.add(order.getItemShipping());
				
				
			    orderCount++;
			    
				purchasedItemCount +=  order.getPurchasedItemCount();
			    soldItemCount += order.getSoldItemCount();
			    sampleItemCount += order.getSampleItemCount();
			    personalUseItemCount += order.getPersonalUseItemCount();
			    inventoryItemCount += order.getInventoryItemCount();
			    demoItemCount += order.getDemoItemCount();
			}
			
		}
		
		
		System.out.println("Finished report");
		
	}

	
}
