package Nuskin;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="commission")
public class Commission {

	@Id
	@GeneratedValue
	Long id;
	
	@Column
	LocalDate date;
	
	@Column
	BigDecimal amount;
	
	@Column
	String description = null; // Not used

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	// Need to take a String in order to replace commas in values > $1000 - the Jackson code calls 
	// this function directly when creating from Json in the HTTP request
	public void setAmount(String sAmount) {
		this.amount = new BigDecimal(sAmount.replaceAll(",", ""));
	}

}
