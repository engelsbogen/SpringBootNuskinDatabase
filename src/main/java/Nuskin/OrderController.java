package Nuskin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;



class OrderUploadResponse {
	
	String message;
	
	List<Product> unknownItems;

	public String getMessage() {
		return message;
	}

	public List<Product> getUnknownItems() {
		return unknownItems;
	}
	
}

@RestController
public class OrderController {

	OrderRepository orderRepo;
	
	OrderController(OrderRepository repo) {
		this.orderRepo = repo;
	}
	
	@RequestMapping(value="/orders")
	public Iterable<Order> getOrders() {
	    
		Iterable<Order> orders = orderRepo.findAll();
		
		// Products on the order are marked transient, not loaded from the database
		// Need to get them now so that we can work out if any items still dont have an end user
		for (Order order: orders) {
			order.getProductsFromDatabase();
		}
		
		//List<Order> target = new ArrayList<>();
		//orders.forEach(target::add);  // :: syntax is a method reference
		//orders.forEach( n-> {target.add(n); } );
		return orders;
    }
	
	
	@DeleteMapping(value="/deleteorder")
    public ResponseEntity<?>  deleteOrder(@RequestParam String orderNumber) {
    	
		ProductDatabase db = ProductDatabase.getDB();
		
		Order existing = db.getOrder(orderNumber);
		if (existing != null) {

            existing.deleteFromDatabase();
			
        	return ResponseEntity.ok().body("[]");
    	}
    	else {
    		return ResponseEntity.badRequest().build();
    	}
    }
	
	@RequestMapping("/orderfiledownload")
	public  ResponseEntity<Resource> orderFileDownload(@RequestParam("orderNumber") String orderNumber, Model model) {

		String filename = orderNumber + ".pdf";
		Path path = Paths.get(FileRoot.getRoot() + "Orders/" + filename);
		File file = path.toFile();
		
		if (!file.exists() ) {
			// No PDF, try .TXT instead
			filename = orderNumber + ".txt";
			path = Paths.get(FileRoot.getRoot() + "Orders/" + filename);
			file = path.toFile();
		}
		

		HttpHeaders headers = new HttpHeaders();
	    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
		
		ByteArrayResource resource = null;
		try {
			resource = new ByteArrayResource(Files.readAllBytes(path));
			return ResponseEntity.ok()
		            .headers(headers)
		            .contentLength(file.length())
		            .contentType(MediaType.parseMediaType("application/octet-stream"))
		            .body(resource);
		} catch (IOException e) {
			
			model.addAttribute("orderfilename", filename);
			
			throw new ResponseStatusException(
					  HttpStatus.NOT_FOUND, "Order file \"" + filename + "\"" 
					);
		}
	}
	
    
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
    
    // Function<T,R> - function which takes a single argument of type T and returns an R
    public static Predicate<Product> distinctProductByKey(Function<Product, String> keyExtractor)
    {
        Map<String, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
    
    @PostMapping("/ordertextupload") 
    public OrderUploadResponse orderTextUpload(@RequestBody String text) {
    	
    	
    	ArrayList<Product> unknownProductTypes = new ArrayList<Product>();
    	
    	System.out.println("Parsing order text");
    	
	    Order order = new OrderParser().parseString(text);
	    	    
    	if (order != null) {

    		if (!order.hasAllInfo())
    		{
    		    // Something went wrong with the parsing.
    			throw new ResponseStatusException(
  					  HttpStatus.UNPROCESSABLE_ENTITY, "Order text is corrupt or incomplete" 
  					);

    		}
    		else {
        		unknownProductTypes = order.getUnknownProductTypes();

        		// TODO make a PDF file from the text 
        		
				Path path = Paths.get(FileRoot.getRoot() + "/Orders/" + order.getOrderNumber() + ".txt");
				
				byte[] b = text.getBytes();
				try {
					Files.write(path, b);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
        		
    		}
    	}
    	
    	OrderUploadResponse resp = new OrderUploadResponse();
    	
    	resp.message = "Order uploaded";
    	resp.unknownItems = 	 unknownProductTypes.stream()
    	                   .filter( distinctProductByKey(p -> p.getSku()) )
    			           .collect(Collectors.toList());

    	
    	return resp;
    	

    }
    
	// POST 
    @PostMapping("/orderfileupload")
    public OrderUploadResponse orderFileUpload(@RequestParam("file") MultipartFile file) {
    	
    	
    	ArrayList<Product> unknownProductTypes = new ArrayList<Product>();
    	
    	System.out.println("Uploading file " + file.getOriginalFilename());

    	Order order = null;
    	
    	// Parse PDF
    	if (file.getContentType().equals("application/pdf")) {
    		
    		try {
			    order = new OrderParserPDF().parse(file.getInputStream());
				
				File dest = new File(FileRoot.getRoot() + "/Orders/" + order.getOrderNumber() + ".pdf");
				
				file.transferTo(dest);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    	else if (file.getContentType().equals("text/plain"))
    	{
    		
		    try {
				order = new OrderParser().parse(file.getInputStream());
				File dest = new File(FileRoot.getRoot() + "/Orders/" + order.getOrderNumber() + ".txt");
				
				file.transferTo(dest);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    	}
    	else {
    		System.out.println(file.getContentType());
    	}
    	
    	if (order != null) {

    		if (!order.hasAllInfo())
    		{
    		    // Something went wrong with the parsing.
    			throw new ResponseStatusException(
  					  HttpStatus.UNPROCESSABLE_ENTITY, file.getOriginalFilename() + " is corrupt or incomplete" 
  					);

    		}

    		else {
        		unknownProductTypes = order.getUnknownProductTypes();
    		}
    	}
    	
    	//https://www.nuskin.com/content/nuskin/en_CA/cpm/order-details.html
    	
    	OrderUploadResponse resp = new OrderUploadResponse();
    	
    	resp.message = "Order uploaded";
    	// Remove duplicates from unknown items
    	resp.unknownItems = 	 unknownProductTypes.stream()
    	                   .filter( distinctProductByKey(p -> p.getSku()) )
    			           .collect(Collectors.toList());

    	
    	return resp;
    }

	    
}
