package Nuskin;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Product  {

	@Id
	@GeneratedValue
	Long id;
	@Column
	String description;
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
	int receiptNumber;
	@Column
	String customerName = "";
	
	@OneToOne // The primary key of productType (the SKU) will be added to this table
	ProductType productType = new ProductType();

	@OneToOne
	Order order;

	@JsonGetter(value="orderNumber") 
	public String getOrderNumber() {
		return order.getOrderNumber();
	}
	
	public BigDecimal getTax() {
		return tax;
	}

	final String TAB = "\t";
	
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
		return costPoints;
	}

	public void setCostPoints(BigDecimal costPoints) {
		this.costPoints = costPoints;
	}

	public BigDecimal getSellingPrice() {
		return sellingPrice;
	}

	public void setSellingPrice(BigDecimal sellingPrice) {
		this.sellingPrice = sellingPrice;
	}

	public int getReceiptNumber() {
		return receiptNumber;
	}

	public void setReceiptNumber(int receiptNumber) {
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

	Product(String SKU, String description, BigDecimal costPrice, BigDecimal costPoints, BigDecimal psv) {
		
		// Find productType in the database by SKU 
		productType = ProductDatabase.getDB().findProductType(SKU);

		this.costPrice = costPrice;
		this.costPoints = costPoints;
		
		// Check description
		this.description = description;
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


	Product(Product rhs) {
		description = rhs.description;
		order = rhs.order;
		costPrice = rhs.costPrice;
		costPoints = rhs.costPoints;  // If product points were used to buy this product then costPrice is zero
	    tax = rhs.tax;
	    shipping = rhs.shipping;
		productType = rhs.productType;
		
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
		
		if (costPrice.compareTo(new BigDecimal("0.0")) == 0) {
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
