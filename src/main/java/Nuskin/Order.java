package Nuskin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="orders")
class Order {
	
	@Id
	@Column(unique=true)
	String orderNumber = "";
	@Column
	String account = "";
	@Column
	String date = "";
	@Column
	String shippingAddress = "";
	@Column
	BigDecimal subtotal = new BigDecimal(0);
	@Column
	BigDecimal shipping = new BigDecimal(0);
	@Column
	BigDecimal tax = new BigDecimal(0);

	@Transient
	ArrayList<Product> products = new ArrayList<Product>();
	
	@Transient 
	Account accnt = null;

	@Transient
	final BigDecimal oneCent = new BigDecimal("0.01");
	
	@JsonGetter(value="hasUnsoldItems") 
	public boolean hasUnsoldItems() {
		
		for (Product product: products) {
			
			if (product.getEndUse() == Product.EndUse.INSTOCK) {
				return true;
			}
		}
		
		// No unsold items found
		return false;
		
	}

	public String getMonth() {
		LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern("M/d/uuuu"));
		
		YearMonth yandm = YearMonth.from(d);
		
		return yandm.toString();
		
	}
	
	public String getAccountName() {
		
		if (accnt == null) {
			accnt = ProductDatabase.getDB().getAccount(account);
		}
		
		return accnt.getAccountName();
		
	}

	@JsonIgnore
	public String getMonthName() {

		LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern("M/d/uuuu"));
		
		String s = d.format(DateTimeFormatter.ofPattern("MMM"));
		
		return s;
		
	}
	
	
    // Function<T,R> - function which takes a single argument of type T and returns an R
    public static Predicate<Product> distinctProductByKey(Function<Product, String> keyExtractor)
    {
        Map<String, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
	
	public String getItemSummary() {
		
		final StringBuilder sb = new StringBuilder();
/*
		// Group items with the same SKU together
		Map<String, List<Product>> map = products.stream().collect(Collectors.groupingBy( Product::getSku));
		
		// Counting
		map.forEach( (k,v) -> { 
			if (sb.length() > 0) sb.append(",");
		    sb.append(v.get(0).getDescription());
		    
		    if (v.size() > 1) {
		    	sb.append( " [x" + v.size() + "]");
		    }
		
		});
*/
		// Alternatively, group items with the same DESCRIPTION together
		Map<String, Long> descMap = products.stream().collect(Collectors.groupingBy(Product::getDescription, Collectors.counting()));
		
		descMap.forEach((desc, count) -> {
			
			if (sb.length() > 0) sb.append(",");
		    sb.append(desc);
		    
		    if (count > 1) {
		    	sb.append( " [x" + count + "]");
		    }
			
		});
		
	
		return sb.toString();
	}
	

	// Provide getters and setters for JSON conversion
	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getShipping() {
		return shipping;
	}

	public void setShipping(BigDecimal shipping) {
		this.shipping = shipping;
	}

	public BigDecimal getTax() {
		return tax;
	}

	public void setTax(BigDecimal tax) {
		this.tax = tax;
	}

	
	public ArrayList<Product>  getUnknownProductTypes() {
		
		ArrayList<Product> itemsWithUnknownProductType = new ArrayList<Product>();
		
		for (Product product: products) {
			
			if (product.getProductType() == null) {
				itemsWithUnknownProductType.add(product);
			}
			
		}
		return itemsWithUnknownProductType;
	}
	
	public boolean hasUnknownProductTypes() {
		
		for (Product product: products) {
			
			if (product.getProductType() == null) {
				return true;
			}
			
		}
		return false;
	}
	
	
	BigDecimal getTotal() {
		BigDecimal total = subtotal;
		total = total.add(shipping);
		total = total.add(tax);
		return total;
	}
	
	BigDecimal getTaxOnShipping() {
		return shipping.multiply(getTaxRate())
                       .setScale(2, RoundingMode.HALF_UP);
	}
	
	
	BigDecimal getShippingIncTax() {
		// Shipping is also subject to tax
		return shipping.add(getTaxOnShipping());
	}
	
	BigDecimal getItemCost() {
		BigDecimal sum = BigDecimal.ZERO;
		
		for (Product p : products) {
			sum = sum.add(p.getCostPrice());
		}
		
		if (sum.compareTo(this.getSubtotal()) != 0) {
			System.err.println(orderNumber + ": sum of item cost " + sum + " is not equal to order subtotal " + getSubtotal());
		}
		
		return sum;
	}
	BigDecimal getItemTax() {
		BigDecimal sum = BigDecimal.ZERO;
		
		for (Product p : products) {
			sum = sum.add(p.getTax());
		}
		
		BigDecimal itemTax = getTax().subtract(getTaxOnShipping());
		
		if (sum.compareTo(itemTax) != 0) {
			System.err.println(orderNumber+ ": sum of item tax " + sum + " is not equal to order tax " + itemTax);
		}
		return sum;
	}
	
	BigDecimal sumItemAndShippingTax() {
		
		BigDecimal taxSum = getItemTax();
		
		// Shipping is also subject to tax
		taxSum = taxSum.add(getTaxOnShipping());
		
		return taxSum;
	}
	
	
	BigDecimal getItemShipping() {
		BigDecimal sum = BigDecimal.ZERO;
		
		for (Product p : products) {
			sum = sum.add(p.getShipping());
		}
		
		BigDecimal orderShipping = this.getShippingIncTax();
		
		if (sum.compareTo(orderShipping) != 0) {
			// We might be one cent out for each item
			System.err.println(orderNumber + ": sum of item shipping " + sum + " is not equal to order shipping " + orderShipping);
		}
		return sum;
	}

	
	BigDecimal getSales() {
		
		BigDecimal sales = BigDecimal.ZERO;
		for (Product product: products) {
			
			if (product.getEndUse() == Product.EndUse.SOLD) {
				sales = sales.add(product.getSellingPrice());
			}
		}

		return sales;
		
	}
	
	BigDecimal getCost(Product.EndUse endUse) {
		
		// Cost of items still in stock
		BigDecimal val = BigDecimal.ZERO;
		for (Product product: products) {
			
			if (product.getEndUse() == endUse) {
				val = val.add(product.getTotalCost());
			}
		}
		return val;
	}
	
	BigDecimal getSalesCost() {
		return getCost(Product.EndUse.SOLD);
	}
	BigDecimal getInventoryCost() {
		return getCost(Product.EndUse.INSTOCK);
	}
	BigDecimal getSampleCost() {
		return getCost(Product.EndUse.SAMPLE);
	}
	BigDecimal getPersonalUseCost() {
		return getCost(Product.EndUse.PERSONAL);
	}
	BigDecimal getDemoCost() {
		return getCost(Product.EndUse.DEMO);
	}
	
	int getItemCount(Product.EndUse endUse) {
		
		int count = 0;
		
		for (Product product: products) {
			
			if (product.getEndUse() == endUse) {
				count++;
			}
		}
		return count;
		
	}
	
	int getPurchasedItemCount() {
		return products.size();
	}
	int getSoldItemCount() {
		return getItemCount(Product.EndUse.SOLD);
	}
	int getInventoryItemCount() {
		return getItemCount(Product.EndUse.INSTOCK);
	}
	int getSampleItemCount() {
		return getItemCount(Product.EndUse.SAMPLE);
	}
	int getPersonalUseItemCount() {
		return getItemCount(Product.EndUse.PERSONAL);
	}
	int getDemoItemCount() {
		return getItemCount(Product.EndUse.DEMO);
	}
	
	void addProduct(Product p, int quantity) {
		
		p.order = this;
		
		for (int i =0 ; i < quantity; i++) {
			
			Product p1 = new Product(p);
			
			products.add(p1);
		}
	}
	
	void applyShippingToProducts() {

		if (shipping.compareTo(BigDecimal.ZERO) != 0) {
			int numberOfItems = products.size();
			// Shipping is subject to tax
			// Need to set a scale and a rounding for BigDecimal.divide otherwise can get arithmetic exceptions
			BigDecimal shippingCostPerItem = getShippingIncTax().divide(new BigDecimal(numberOfItems), 2, RoundingMode.HALF_UP);
			                                                    
			for (Product product: products) {
				product.setShipping(shippingCostPerItem);
			}
		}
	}


	
	private BigDecimal getTaxRate() {
		
		// HST rate depends on the shipping address
		// For Ontario, its 13%
		// New Brunswick is 15%
		// Others - TODO
		BigDecimal taxRate = new BigDecimal("0.15");
		
		if (shippingAddress.contains(" NB,")) {
			//taxRate = new BigDecimal("0.13");
		}
		else if (shippingAddress.contains(" ON,")) {
			taxRate = new BigDecimal("0.13");
		}
		
		return taxRate;
		
	}
	
	void applyTaxToProducts() {
		
		// If this is a preferred customer account, tax is % of the wholesale price
		// If its a distributor account, tax is % of the retail price
		// This applies also to product bought with points
		
		// First can I work out if its a distributor account from the order, or do I just
		// go off knowing the account number?
		BigDecimal taxSum = new BigDecimal(0);
		boolean isDistributorAccount = account.startsWith("CA6");
		
		BigDecimal taxRate = getTaxRate();
		
		for (Product product: products) {
			product.calculateTax(isDistributorAccount, taxRate);
		}

		taxSum = sumItemAndShippingTax();
		
		BigDecimal error = taxSum.subtract(getTax()).abs();

		if (error.compareTo(oneCent) > 0) {
		
			System.err.println("Expected tax " + taxSum + " Actual tax charged " + tax);
			
			// OK, something went wrong. Maybe a product price has changed or is wrong in the database
			
			// This can happen if either or both:
			//  (a) products were purchased with points. Tax is charged on retail or wholesale price
			//      Its also possible that products bought with points on a distributor account are taxed on the wholesale price 
			//      - that seems to get close to (but not exactly) the actual tax charged on at least one order
			//  (b) order made on a distributor account, cost is wholesale price but tax is charged on the retail price
			
			// Under these circumstances we don't have sufficient information on the order, as it does not itemise tax
			// so we are relying on the product type (SKU) database to provide the price information. If the price has 
			// changed, or perhaps a special offer, then it won't match.
			
			// For case (a), maybe we could compare the points cost with the database - a price change may also imply a
			// points value change
			// I can't know which item or items are wrong, will instead simply make an adjustment to each product, as 
			// we don't need to account for GST/HST to CRA, just need to know how much we should be charging to cover
			// our costs.

			// To avoid ArithmeticException: Non-terminating decimal expansion; no exact representable decimal result
			// have to provide scale and rounding mode. Use sufficient decimal places (6 seems ok) to get the accuracy to 1 penny.

			// We need to REMOVE the shipping tax as we know what that is and we've added it to the 
			// shipping cost per item. Adjust only the item tax.
			BigDecimal a = tax.subtract(this.getTaxOnShipping());
			BigDecimal b = getItemTax();
			
			BigDecimal adjustRatio = a.divide(b, 8, RoundingMode.HALF_UP);
	
			for (Product product: products) {
				BigDecimal newTax = product.getTax().multiply(adjustRatio);
				product.setTax(newTax.setScale(2, RoundingMode.HALF_UP));
			}

			// Might be pennies out still
			taxSum = sumItemAndShippingTax();
			
			if (taxSum.compareTo(tax) != 0 ) {
				System.err.println("Adjusted expected tax " + taxSum + " Actual tax charged " + tax);
			}
			else {
				System.err.println("After adjustment, sum = actual tax charged = " + tax);
			}
		}
		else {
			System.err.println("Expected tax matches actual tax charged: " + tax);
		}
		
	}
	
	
	boolean isSet(String s) {
		
		return !(s == null || s.length() == 0); 
	}
	
	
	boolean hasAllInfo() {
		
		boolean complete = true;
		
		if (!isSet(orderNumber)) complete = false;
		if (!isSet(account)) complete = false;
		if (!isSet(date)) complete = false;
		if (!isSet(shippingAddress)) complete = false;
		if (subtotal.compareTo(BigDecimal.ZERO) == 0 ) complete = false;
		if (tax.compareTo(BigDecimal.ZERO) == 0) complete = false;
		// shipping may be zero

		// Must have at least one product item 
		if (products.isEmpty()) complete = false;
		
		
		return complete;
	}
	
	void show() {
		System.out.println("Order number: " + orderNumber);
		System.out.println("Account     : " + account);
		System.out.println("Date        : " + date);
		System.out.println("Subtotal    : $" + subtotal);
		System.out.println("Shipping    : $" + shipping);
		System.out.println("Tax         : $" + tax);
		System.out.println("Total       : $" + getTotal());

		for (Product product : products) {
			product.show();
		}
	}
	
	
	void getProductsFromDatabase() {
		
		ProductDatabase db = ProductDatabase.getDB();
		products = db.getOrderItems(this.orderNumber); 

		// Make sure they are sorted by ID so they are in the same order as 
		// when the Order was added to the database
		Collections.sort( products, (p1, p2) -> { return (int)(p1.getId() - p2.getId()); });
		
	}
	
	
	
	void correctTaxAndShipping() {
		
		BigDecimal itemShipping = getItemShipping();
		
		boolean fixNeeded  = false;

		// Fix shipping first, as there was a gross error in the calculation of the tax
		// on shipping
		
		if (this.getShippingIncTax().compareTo(itemShipping) != 0 ) {
			fixNeeded = true;
			System.err.println("Fixing shipping");
			applyShippingToProducts();
		}

		BigDecimal taxOnOrder = getTax();
		BigDecimal taxOnItems = sumItemAndShippingTax(); // includes tax on shipping
		
		// If its 1 cent its probably not worth bothering
		BigDecimal error = taxOnOrder.subtract(taxOnItems).abs();
			
		if (error.compareTo( oneCent ) > 0 ) {
			fixNeeded = true;
			System.err.println("Fixing tax");
			applyTaxToProducts();
		}

		if (fixNeeded) {
		
			ProductDatabase db = ProductDatabase.getDB();
	
			//db.addOrder(this);  // Not modifying this, just the items' tax and shipping
			
			// Add each product to the database
			for (Product product : products) {
				db.addProduct(product);
			}
		}
	}
	
	void addToDatabase() {
		ProductDatabase db = ProductDatabase.getDB();
		
		
		// Is this order already in the database?
		
		Order existing = db.getOrder(this.orderNumber);
		if (existing == null) {
			db.addOrder(this);
		
			// Add each product to the database
			for (Product product : products) {
				db.addProduct(product);
			}
		}
		else {
			
			System.out.println("Order already in the database");
			
			if (!this.date.equals(existing.date)) {
				System.err.println("Dates don't match");
			}
			if (!this.account.equals(existing.account)) {
				System.err.println("Accounts don't match");
			}
			if (!this.shippingAddress.equals(existing.shippingAddress)) {
				System.err.println("Shipping addresses don't match");
			}
			if (this.subtotal.compareTo(existing.subtotal) != 0) {
				System.err.println("Subtotals don't match");
			}
			if (this.tax.compareTo(existing.tax) != 0) {
				System.err.println("Subtotals don't match");
			}
			if (existing.products.size() != this.products.size()) {
				System.err.println("Order has different number of items");
			} 
			else {
				for (int i=0; i< products.size(); i++) {
					existing.products.get(i).compare(this.products.get(i));
				}
			}
		
		}
	}
	

	
}
