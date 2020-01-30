package Nuskin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
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

    
    ProductAndQuantity parseProductNew(BufferedReader reader, String SKU, String lastLine) {
        
        ProductAndQuantity p =  new ProductAndQuantity();
    
        if (startsWithAnSKU(SKU)) {
             
          try {
             String description = lastLine;
             
             // PSV: 
             String line = reader.readLine().trim(); 
             BigDecimal PSV = new BigDecimal(line.substring(5));
             
             // Now they've added price here
             line = reader.readLine();
             
             int quantity;
             @SuppressWarnings("unused")
             BigDecimal price;
             PriceOrPoints pp;
             try {
                 // Another change - they've started to put the unit price before the quantity
                 // In that case, parsing it as an int will throw an exception, which we catch 
                 // and continue below
                 quantity = Integer.parseInt(line);
                 // Ok, that succeeded, continue with the old type processing
                 String unitPriceOrPoints = reader.readLine().trim();
                 pp = parsePriceOrPoints(unitPriceOrPoints);
                 
                 //String totalPriceOrPoints = parts[5];
                 
                 // The POINTS value is the total so have to divide by the the quantity to get the points per item
                 pp.points = pp.points.divide(new BigDecimal(quantity));
             }
             catch(NumberFormatException e) {
                 
                 // So it wasn't an integer, parse it as a $ price
                 price = parseDollars(line);
                 line = reader.readLine();
                 quantity = Integer.parseInt(line);
                 String unitPriceOrPoints = reader.readLine().trim();
                 pp = parsePriceOrPoints(unitPriceOrPoints);
                 
                 // The POINTS value is the total so have to divide by the the quantity to get the points per item
                 pp.points = pp.points.divide(new BigDecimal(quantity));
                 // Also in this format the price is the total price 
                 pp.price = pp.price.divide(new BigDecimal(quantity));
             }
            
         
             // Tax is 15%
             // Shipping should divide equally among all products
             
             p.product = new Product(SKU, description, pp.price, pp.points, PSV);
             p.quantity = quantity;
            }
            catch(IOException e)
            {
                
            }
         }
         
        return p;
        
    }

    
	BigDecimal parseDollars(String s) {
		// Remove commas if the order is > $1000!
		String ss = s.substring(s.indexOf('$')+1);
		ss = ss.replaceAll(",", "");
		return  new BigDecimal(ss);
	}
	
	
	
	public Order parseString(String text) {
		
        BufferedReader reader = new BufferedReader(new StringReader(text));
	
		return parse(reader);
		
	}
	
	public Order parse(InputStream strm)
	{
        BufferedReader reader = new BufferedReader(new InputStreamReader(strm));
        
        return parse(reader);
        
	}

    public Order parse(String filename)
    {
        BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
	        return parse(reader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	return null;
    }
   

    boolean isAccount(String s)
    {
        if (s.startsWith("CA") || s.startsWith("US") || s.startsWith("UK")) {
            
            // Then expect some numbers
            if (s.length() >= 8 && s.length() <= 10)  {
                
                try {
                    int ac = Integer.parseInt(s.substring(2));
                    if (ac > 0) return true;
                }
                catch (NumberFormatException e) {
                    
                }
                
            }
            
        }
        
        return false;
    }
    
    public Order parseChrome(BufferedReader reader)
    {
	    Order order = new Order();
        try {
	    	
	    	String line = reader.readLine();
	    	
	    	while(line != null) {

	    		line = line.trim();
	    		
  		
	    		if (line.startsWith("Order Number:" )) {
	    			//Order Number: 0237947036 PRINT Order Date:6/26/2019 Order Channel:Web Order Status:Complete

	    	   	 	Pattern pattern= Pattern.compile("Order Number: ([0-9]{10}+).*Order Date:([0-9\\/]*) ");
	    	   	 	Matcher m = pattern.matcher(line);
	    	   	 	if (m.lookingAt()) {
	    	   	 		order.orderNumber = m.group(1);
	    	   	 		order.date = m.group(2);
	                    order.date = Util.checkDateFormat(order.date);
	    	   	 	}
	    			//order.orderNumber = line.substring(line.indexOf(':') + 2, 10);
    			
	    		}
	    		
	    		else if (line.startsWith("ID:")) {
	    			// Account number on next line
	    			order.account =	 reader.readLine().trim();
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
	    			// next line is the shipping address
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
        }
	    catch (IOException e) {
	    	e.printStackTrace();
	    }
		
	    return order;
    }
    
    public Order parseFirefox(BufferedReader reader) {

    	Order order = new Order();
	    
        try {
	    	
	    	String line = reader.readLine();
	    	
	    	while(line != null) {

	    		line = line.trim();
 		
   		
	    		if (line.startsWith("Order Number" )) {
	    			order.orderNumber = line.substring(line.indexOf(':') + 2);
	    		}
	    		
	    		if (line.startsWith("Order Date")) {
	    			// Date is on the next line
	    			order.date = reader.readLine().trim();
         			order.date = Util.checkDateFormat(order.date);
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
    	}
	    catch (IOException e) {
	    	e.printStackTrace();
	    }
	    
	    return order;
    	
    }
    
    
    public Order parseNewFormat(BufferedReader reader, String account)
    {
        Order order = new Order();

        order.account = account;
        try {
            
            String lastLine = ""; 
            String line = reader.readLine();
            
            while(line != null) {

                line = line.trim();
        
                if (line.startsWith("Order #:")) {
                    order.orderNumber = line.substring(9);
                }
                else if (line.startsWith("Date:")) {
                    order.date = line.substring(6);
                    order.date = Util.checkDateFormat(order.date);
                }
                else if (line.startsWith("Shipped to:")) {
                    // next line is name, then 2 lines after (always?) are the shipping address
                    order.shippingAddress = reader.readLine();
                    order.shippingAddress += " ";
                    order.shippingAddress += reader.readLine().trim();
                    order.shippingAddress += ", ";
                    order.shippingAddress += reader.readLine().trim();
                }
                //Subtotal:  $358.25
                else if (line.startsWith("Subtotal")) {
                    order.subtotal = parseDollars(line);
                }
                //Shipping:  $0.00
                else if (line.startsWith("Shipping") && line.contains("$")) {
                    order.shipping = parseDollars(line);
                }
                //Tax:   $81.25
                else if (line.startsWith("Tax")) {
                    order.tax = parseDollars(line);
                    
                    // Orders through a distributor account attract tax on the retail price,
                    // and also tax is applied to products purchased with points. 
                    // As the order does not itemise the tax we need to know what the wholesale price of each 
                    // product is to know how to split up the cost
                    
                }
                //Total:     $439.50             
                else if (line.startsWith("Total:")) {
                    if (order.getTotal().compareTo(parseDollars(line)) != 0) {
                        throw new RuntimeException();
                    }
                }
                else {
                    
                    // Products are now
                    //  Description
                    //  SKU
                    //  PSV: 
                    //  Quantity
                    //  Unit price
                    //  Total price
                    // So can't identify the start by looking for an SKU anymore.
                    // Keep the last line then once we find an SKU, use it.
                    ProductAndQuantity p = parseProductNew(reader, line, lastLine);
                    if (p.quantity > 0) order.addProduct(p.product, p.quantity);
                }
                lastLine = line;
                line = reader.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return order;
    }
        
	public Order parse(BufferedReader reader)
	{
		
		// Firefox and Chrome behave differently
		
	    Order order = new Order();
	    
        try {
	    	
	    	String line = reader.readLine();
	    	
	    	while(line != null) {

	    		line = line.trim();
	    		// Ctrl-A/Ctrl-C on Chrome first line is "Name:" and <name> is on the next line
	    		// On Firefox, we get "    Name:<name>" on the second line
	    		// So if we see a line just "Name:" then assume its from Chrome, else Firefox
	    		if (line.startsWith("Name:")) {
	    			if (line.endsWith("Name:"))
	    				order = parseChrome(reader);
	    			else
	    				order = parseFirefox(reader);
	    			break;
	    		}
	    		else if (isAccount(line)) {
	    		    order = parseNewFormat(reader, line);
	    		    break;
	    		}
	    		
	    		line = reader.readLine();
	    	}
	    	
	    	reader.close();
	    }
	    catch (IOException e) {
	    	e.printStackTrace();
	    }
		

        if (!order.hasUnknownProductTypes() && order.hasAllInfo()) {
	    	// Divi up this orders shipping cost over all the products ordered
	    	order.applyShippingToProducts();
	    	
	    	// Work out the tax paid on each product
	    	order.applyTaxToProducts();
	        // Add this order to the database
		    order.addToDatabase();
        }
		
	    return order;
	    
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
