package Nuskin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="accounts")

public class Account {

	@Id
	@Column(unique=true)
	String accountNumber;
	
	@Column
	String accountName;
	
	@Column
	boolean isDistributorAccount;

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public boolean isDistributorAccount() {
		return isDistributorAccount;
	}

	public void setDistributorAccount(boolean isDistributorAccount) {
		this.isDistributorAccount = isDistributorAccount;
	}
	
	
}
