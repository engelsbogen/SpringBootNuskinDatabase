package Nuskin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;

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
	
	public String getItemSummary() {
		
		String s = "";
		
		for (Product p : products) {

			if (s.length() > 0) s+= ","; 
			
			s += p.getDescription();
			
		}
		
		return s;
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
	
	void addProduct(Product p, int quantity) {
		
		p.order = this;
		
		for (int i =0 ; i < quantity; i++) {
			
			Product p1 = new Product(p);
			
			products.add(p1);
		}
	}
	
	void applyShippingToProducts() {
		int numberOfItems = products.size();
		BigDecimal shippingCostPerItem = shipping.divide(new BigDecimal(numberOfItems));
		for (Product product: products) {
			product.setShipping(shippingCostPerItem);
		}
	}

	void applyTaxToProducts() {
		
		// If this is a preferred customer account, tax is % of the wholesale price
		// If its a distributor account, tax is % of the retail price
		// This applies also to product bought with points
		
		// First can I work out if its a distributor account from the order, or do I just
		// go off knowing the account number?
		BigDecimal taxSum = new BigDecimal(0);
		boolean isDistributorAccount = account.startsWith("CA6");
		
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
		
		for (Product product: products) {
			product.calculateTax(isDistributorAccount, taxRate);
			taxSum = taxSum.add(product.getTax());
		}
		
		if (taxSum.compareTo(tax) != 0 ) {
			System.err.println("Expected tax " + taxSum + " Actual tax charged " + tax);
		}
		else {
			System.err.println("Expected tax " + taxSum + " Actual tax charged " + tax);
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
		// Make sure they are sorted by item number 
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
