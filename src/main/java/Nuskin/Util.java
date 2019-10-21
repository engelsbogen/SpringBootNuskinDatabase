package Nuskin;

import java.time.Month;
import java.time.YearMonth;
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
	
	static String checkDateFormat(String date) {
        // Can we determine what the date format is?
        // Assume MM/DD/YYYY to start with?
        // If MM > 12  ||  (YYYY = current year && MM > current month) 
        //   probably DD/MM/YYYY, swap day and month
        // But I cant see a proper way. There is nothing in the file that tells me
        // Is 04/05/2019 April 5th or May 4th?
        String[] parts = date.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[2]);
        
        if (month > 12 || (year == YearMonth.now().getYear() && month > YearMonth.now().getMonthValue()))
        {
            date = parts[1] + "/" + parts[0] + "/" + parts[2];
        }
	    return date;
	}

	
}
