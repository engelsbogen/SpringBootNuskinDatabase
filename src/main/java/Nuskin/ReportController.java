package Nuskin;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

	@RequestMapping(value="/report")
	public Report getReport(@RequestParam String period) {
		
		
		Report report = new Report();

		// Period is for example "year", "January", "Jan-Apr", "month"
		report.build(period);
		
		return report;
		
	}

	
}
