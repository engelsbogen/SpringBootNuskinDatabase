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
	String description = null;

}
