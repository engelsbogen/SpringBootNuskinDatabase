package Nuskin;

import java.time.Month;
import java.time.format.DateTimeFormatter;

public class Util {

	
	static int periodToMonthValue(String period) {
        return Month.from(DateTimeFormatter.ofPattern("MMM").parse(period)).getValue();
	}
	
	static String periodToLongName(String period) {
		
		if (period.equals("year")) return "year to date";
		else {
			String[] months= {"year to date", 
				          "January", "February", "March", 
				          "April", "May", "June", 
				          "July", "August", "September",
				          "October", "November", "December" };
			int val = periodToMonthValue(period);
			return months[val];
		}
	}
	

	
}
