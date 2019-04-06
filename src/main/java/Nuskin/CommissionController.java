package Nuskin;

import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommissionController {

	
	
	@Autowired
	CommissionRepository commissionRepo;
	
    @PutMapping("/addcommission")
    public ResponseEntity<?>  addCommission(@RequestBody Commission newCommission) {
    	
		commissionRepo.save(newCommission);
    	
		// Flush the changes  
		commissionRepo.flush();
		
        // Adding .body("[]") makes it return a JSON response
    	return ResponseEntity.ok().body("[]");

    }

  
	

    @DeleteMapping(value="/deletecommission")
    public ResponseEntity<?>  deleteCommission(@RequestParam Long id) {
    	
    	if (commissionRepo.existsById(id)) {
    	
    		commissionRepo.deleteById(id);
    	
    		//	Flush the changes  
    		commissionRepo.flush();

    		// Adding .body("[]") makes it return a JSON response
        	return ResponseEntity.ok().body("[]");
    	}
    	else {
    		return ResponseEntity.badRequest().build();
    	}
    }
    
    
	@RequestMapping(value="/commission")
	public CommissionReport getReport(@RequestParam String period) {
		
		CommissionReport report = new CommissionReport();

		// Period is for example "year", "January", "Jan-Apr", "month"
		report.build(period);
		
		return report;
		
	}
	
}
