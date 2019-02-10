package Nuskin;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Embeddable
public class ProductType {

	@Id
	@Column(unique=true)
	String sku = "";
	@Column
	String description = "";
	@Column
	BigDecimal retailPrice = new BigDecimal(0);
	@Column
	BigDecimal wholesalePrice = new BigDecimal(0);
	@Column
    BigDecimal psv = new BigDecimal(0);
	
	public String getSku() {
		return sku;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getRetailPrice() {
		return retailPrice;
	}

	public BigDecimal getWholesalePrice() {
		return wholesalePrice;
	}

	public BigDecimal getPsv() {
		return psv;
	}

	public BigDecimal getCsv() {
		return csv;
	}
	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}

	public void setWholesalePrice(BigDecimal wholesalePrice) {
		this.wholesalePrice = wholesalePrice;
	}

	public void setPsv(BigDecimal psv) {
		this.psv = psv;
	}

	public void setCsv(BigDecimal csv) {
		this.csv = csv;
	}


	@Column
    BigDecimal csv = new BigDecimal(0);
	
	
	ProductType() {
		
	}
	
    // Product pricing information from NuSkin price list
	ProductType(String SKU, String description, BigDecimal retailPrice, BigDecimal wholesalePrice, BigDecimal PSV, BigDecimal CSV) {
		this.sku = SKU;
		this.description = description;
		this.retailPrice = retailPrice;
		this.wholesalePrice = wholesalePrice;
		this.psv = PSV;
		this.csv = CSV;
	}
	
	
	void show() {
		System.out.println(sku  
				           + "\t\'" + description + "'"
				           + "\t Retail $" + retailPrice
				           + "\t Wholesale $" + wholesalePrice 
				           + "\t PSV " + psv 
				           + "\t CSV " + csv);
	}


	
}
