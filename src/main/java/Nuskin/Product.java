package Nuskin;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Product  {

	@Id
	@GeneratedValue
	Long id;
	@Column
	String description = "";
	@Column
	BigDecimal costPrice = new BigDecimal(0);
	@Column
	BigDecimal costPoints = new BigDecimal(0);  // If product points were used to buy this product then costPrice is zero
	@Column
    BigDecimal tax = new BigDecimal(0);
	@Column
    BigDecimal shipping = new BigDecimal(0);
	@Column
	BigDecimal sellingPrice = new BigDecimal(0);
	@Column 
	String receiptNumber = "";
	@Column
	String customerName = "";
	@Column
	EndUse endUse = EndUse.INSTOCK;
	@OneToOne // The primary key of productType (the SKU) will be added to this table
	ProductType productType = new ProductType();
	@OneToOne
	Order order;

	@Transient
	String sku = null;

	static final String TAB = "\t";
	
	enum EndUse {
		INSTOCK, SOLD, PERSONAL, DEMO, SAMPLE 
	}
	
	public EndUse getEndUse() {
		return endUse;
	}

	public void setEndUse(EndUse endUse) {
		this.endUse = endUse;
	}
	
	@JsonGetter(value="orderNumber") 
	public String getOrderNumber() {
		return order.getOrderNumber();
	}
	@JsonGetter(value="sku") 
	public String getSku() {
		return (productType == null ? this.sku : productType.getSku());
	}

	public BigDecimal getTax() {
		return tax;
	}
	
	Product() {
		description = "NONE";
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public BigDecimal getCostPoints() {
		
		if (this.costPoints.compareTo(BigDecimal.ZERO) == 0) {
			return (productType == null ? BigDecimal.ZERO : productType.getPsv());
		}
		else return costPoints;
	}

	public void setCostPoints(BigDecimal costPoints) {
		this.costPoints = costPoints;
	}

	public BigDecimal getSellingPrice() {
		return sellingPrice;
	}

	//public void setSellingPrice(BigDecimal sellingPrice) {
	//	this.sellingPrice = sellingPrice;
	//}
	
	// Need to take a String in order to replace commas in values > $1000 - the Jackson code calls 
	// this function directly when creating from Json in the HTTP request
	public void setSellingPrice(String sSellingPrice) {
		this.sellingPrice = new BigDecimal(sSellingPrice.replaceAll(",", ""));
	}

	public String getReceiptNumber() {
		if (receiptNumber == null) receiptNumber = "";  // Replace with empty string to help out with the front end
		return receiptNumber;
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	@JsonIgnore
	public ProductType getProductType() {
		return productType;
	}

	public void setProductType(ProductType productType) {
		this.productType = productType;
	}

	@JsonIgnore
	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public void setTax(BigDecimal tax) {
		this.tax = tax;
	}
	
	public BigDecimal getShipping() {
		return shipping;
	}

	public void setShipping(BigDecimal shipping) {
		this.shipping = shipping;
	}
	
	public BigDecimal getTotalCost() {
		BigDecimal cost = this.costPrice;
		cost = cost.add(this.tax);
		cost = cost.add(this.shipping);
		return cost;
	}
	
	
	// Check get the same result reading a PDF and a text file
	void compare(Product other) {
		
		// Seems I didn't trim trailing spaces from the text file parsing 
		// (code fixed now but database still has it in)
		this.description = this.description.trim();
		
		if (!this.description.equals(other.description) ) {
			System.err.println("Description does not match," + this.getDescription() + "/" + other.getDescription());
		}
		if (this.costPrice.compareTo(other.costPrice) != 0) {
			System.err.println("Product cost price does not match");
		}
		if (this.shipping.compareTo(other.shipping) != 0) {
			System.err.println("Product shipping does not match");
		}
		if (this.costPoints.compareTo(other.costPoints) != 0) {
			System.err.println("Product cost points does not match");
		}
		if (this.tax.compareTo(other.tax) != 0) {
			System.err.println("Product tax does not match: " + this.getTax() + "/" + other.getTax());
		}
		
	}
	

	Product(String SKU, String description, BigDecimal costPrice, BigDecimal costPoints, BigDecimal psv) {
		
		this.costPrice = costPrice;
		this.costPoints = costPoints;
		this.description = description;
		this.sku = SKU;

		// costPoints is used here to also mean PSV.
		// If the item was bought with points then the cost in points is equal to the product PSV.
		// If the item was bought with cash then the PSV is shown separately on the order
		// Probably, I don't care about the PSV anyway
		if (costPoints.compareTo(BigDecimal.ZERO) ==0 ) this.costPoints = psv;
		
		// Find productType in the database by SKU 
		ProductDatabase db = ProductDatabase.getDB();

		// TODO if running outside of Spring the database connection might not be made
		if (db != null) {
			productType = db.findProductType(SKU);
	
			if (productType == null) {
				
				// Its a new product. 
				// Don't know if this is retail or wholesale price. Anyway need to know both to calculate the tax 
				//BigDecimal retailPrice = costPrice;
				//BigDecimal wholesalePrice = costPrice;
				//BigDecimal csv = new BigDecimal("0.00");
				//productType = new ProductType(SKU, description, retailPrice, wholesalePrice, psv, csv);
				
			}
			else {
				// Check description
				if (!description.equals(productType.description)) {
					System.err.println("Order description not the same as the product description");
				}
				
				// Check cost price matches either wholesale or retail
				if (costPrice.compareTo(BigDecimal.ZERO) == 0) {
					
					// Check that the points cost is the same as the ProductType PSV
					if (costPoints.compareTo(productType.getPsv()) != 0) {
						System.err.println("Product cost in points " + costPoints + " is not the same as the product type PSV " + productType.getPsv());
					}
				}
				else {
					if (   (costPrice.compareTo(productType.getRetailPrice()) != 0)
				    	&& (costPrice.compareTo(productType.getWholesalePrice()) != 0)) {
		
						System.err.println("Product cost " + costPrice + " is not the same as the product type retail price "
		          						+ productType.getRetailPrice() + " or wholesale price " + productType.getWholesalePrice());
						
					}
				}
			}
		}
	}


	Product(Product rhs) {
		description = rhs.description;
		order = rhs.order;
		costPrice = rhs.costPrice;
		costPoints = rhs.costPoints;  // If product points were used to buy this product then costPrice is zero
	    tax = rhs.tax;
	    shipping = rhs.shipping;
		productType = rhs.productType;
		sku = rhs.sku;
	}
	
	void show() {
		
		System.out.println(   productType.sku + TAB 
				            + order.orderNumber + TAB
				            + costPrice  + TAB
				            + costPoints + TAB
				            + tax + TAB
				            + shipping + TAB);
		
	}
	
	void calculateTax(boolean isDistributorAccount, BigDecimal hstRate) {
		
        BigDecimal taxRate = hstRate;
        
		// It appears that G3 juice is zero-rated for tax - what other products might this apply to?
		if (productType.getSku().equals("02003648")) {
			taxRate = BigDecimal.ZERO;
		}
		
		if (costPrice.compareTo(BigDecimal.ZERO) == 0) {
			
			if (costPoints.compareTo(BigDecimal.ZERO) == 0) {
				
				// Neither points nor money - real cost zero hence tax zero
				tax = BigDecimal.ZERO;
			}
			else {
			
				// If bought with product points, tax is charged at what?? Its some function of the point value I think.
				
				// For now go off the retail/wholesale price from the ProductType database, see if that works
				if (isDistributorAccount) {
					// If bought through distrubutor account, tax is charged on the retail price.
					tax = productType.retailPrice.multiply(taxRate);
				}
				else {
					tax = productType.wholesalePrice.multiply(taxRate);
				}
			}
		}
		else {
			if (isDistributorAccount) {
				// If bought through distrubutor account, tax is charged on the retail price.
				tax = productType.retailPrice.multiply(taxRate);
			}
			else {
				// Tax is charged on the cost price - will be the wholesale price for preferred customer, 
				// possibly with a 5% discount if bought on ADR
				tax = costPrice.multiply(taxRate);
			}
		}
		
		// Not sure what rounding mode
		tax = tax.setScale(2, RoundingMode.HALF_UP);
			
	}
	

	
}
