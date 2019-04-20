package Nuskin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

	@RequestMapping(value="/report")
	public Report getReport(@RequestParam String period) {
		
		
		Report report = new Report();

		// Period is for example "year", "Jan", "Jan-Apr", "month"
		report.build(period);
		
		return report;
		
	}

	
}
