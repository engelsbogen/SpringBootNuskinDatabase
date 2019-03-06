package Nuskin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class PriceOrPoints {
	BigDecimal price = new BigDecimal("0.00");
    BigDecimal points = new BigDecimal("0.00");
}

class ProductAndQuantity {
	Product product = null;
	int quantity = 0;
}


public class OrderParser {
	
	PriceOrPoints parsePriceOrPoints(String s) {
		
		PriceOrPoints p = new PriceOrPoints();
	
		String[] parts = s.split("[/ ]");
		p.price = new BigDecimal(parts[0].substring(1));

		if (parts.length > 1) {
			p.points = new BigDecimal(parts[1]);
		}
		
		return p;
	}
	
	
	boolean startsWithAnSKU(String line) {
		// If line starts with an 8 digit SKU then its a product 
   	 	Pattern pattern= Pattern.compile("^[0-9]{8}+");
   	 	Matcher m = pattern.matcher(line);
   	 	return m.lookingAt();
	}
	
    ProductAndQuantity parseProduct(String line) {
    	
    	ProductAndQuantity p =  new ProductAndQuantity();
   	
    	if (startsWithAnSKU(line)) {
    		 
    		 // Split line on tabs
    		 
    		 String[] parts = line.split("\t");
    		 
    		 String SKU = parts[0];
    		 
    		 if (parts.length < 2) {
    			 return p;
    		 }
    		 
    		 String description = parts[1].trim();
    		 int quantity = Integer.parseInt(parts[2].trim());
    		 
    		 String unitPriceOrPoints = parts[3];
   			 PriceOrPoints pp = parsePriceOrPoints(unitPriceOrPoints);
    		 BigDecimal PSV = new BigDecimal(parts[4].trim());
    		 
    		 //String totalPriceOrPoints = parts[5];
    		 
    		 // The POINTS value is the total so have to divide by the the quantity to get the points per item
    		 pp.points = pp.points.divide(new BigDecimal(quantity));
    		 
    		 // Tax is 15%
    		 // Shipping should divide equally among all products
    		 
    		 p.product = new Product(SKU, description, pp.price, pp.points, PSV);
    		 p.quantity = quantity;
    		 
    	 }
    	 
    	return p;
    	
    }
	
	BigDecimal parseDollars(String s) {
		return  new BigDecimal(s.substring(s.indexOf('$')+1));
	}
	
	
	public void parse(String filename)
	{
	    Order order = new Order();
	    
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
	    	
	    	String line = reader.readLine();
	    	
	    	while(line != null) {

	    		line = line.trim();
	    		
	    		if (line.startsWith("Order Number" )) {
	    			order.orderNumber = line.substring(line.indexOf(':') + 2);
	    		}
	    		
	    		if (line.startsWith("Order Date")) {
	    			// Date is on the next line
	    			order.date = reader.readLine().trim();
	    		}
	    		else if (line.startsWith("ID:")) {
	    			order.account = line.substring(3);
	    		}
	    		//Subtotal 	$358.25
	    		else if (line.startsWith("Subtotal")) {
	    			order.subtotal = parseDollars(line);
	    		}
	    		//Shipping 	$0.00
	    		else if (line.startsWith("Shipping") && line.contains("$")) {
    				order.shipping = parseDollars(line);
	    		}
	    		//Shipping 	$0.00
	    		else if (line.startsWith("Shipping Address")) {
	    			// skip next line, then then line after is the shipping address
	    			reader.readLine();
	    			order.shippingAddress = reader.readLine().trim();
	    		}
	    		//Tax 	$81.25
	    		else if (line.startsWith("Tax")) {
	    			order.tax = parseDollars(line);
	    			
	    			// Orders through a distributor account attract tax on the retail price,
	    			// and also tax is applied to products purchased with points. 
	    			// As the order does not itemise the tax we need to know what the wholesale price of each 
	    			// product is to know how to split up the cost
	    			
	    		}
	    		//Total 	$439.50	    		
	    		else if (line.startsWith("Total")) {
	    			if (order.getTotal().compareTo(parseDollars(line)) != 0) {
	    				throw new RuntimeException();
	    			}
	    		}
	    		else {
	    			ProductAndQuantity p = parseProduct(line);
	    			if (p.quantity > 0) order.addProduct(p.product, p.quantity);
	    		}
	    		
	    		line = reader.readLine();
	    	}
	    	
	    	// Divi up this orders shipping cost over all the products ordered
	    	order.applyShippingToProducts();
	    	
	    	// Work out the tax paid on each product
	    	order.applyTaxToProducts();
	    	
	    	
	    	reader.close();
	    }
	    catch (IOException e) {
	    	e.printStackTrace();
	    }
		

        // Add this order to the database
	    order.addToDatabase();
		
	}
	
	
	public void parseAll() {
		
		File orderDir = new File(FileRoot.getRoot() + "Orders");
		
        File[] filesList = orderDir.listFiles();

        for (File f : filesList){
        	parse(f.getAbsolutePath());
        }
       
	}
	
	public static void main(String[] args) {
		OrderParser parser = new OrderParser();
		parser.parseAll();
	}

	
}
