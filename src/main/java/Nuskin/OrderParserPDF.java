package Nuskin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;



public class OrderParserPDF extends OrderParser {

	String readPdfText(String filename) {
	
		String text = "";
		
		File file = new File(filename);
		try {
			FileInputStream stream = new FileInputStream(file);
			text = readPdfText(stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return text;
		
	}
	
	String readPdfText(InputStream stream) {
			
		// Retrieving text from PDF document
		String text = "";
		try {
			PDDocument document = PDDocument.load(stream);

			// Instantiate PDFTextStripper class
			PDFTextStripper pdfStripper = new PDFTextStripper();
			text = pdfStripper.getText(document);

			document.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		return text;
		
	}
	
	
	ProductAndQuantity isFullItemDescription(String line) {
		
    	ProductAndQuantity p =  new ProductAndQuantity();
    	
		// So.
		// We should start with an SKU
		// Followed by a description 
		// Followed by a Qty
		// Followed by unit price $NN.NN 
		//              or unit price /point  $0.00/
   	 	
   	 	String pattSKUCapture = "(^[0-9]{8}+)";                         // 8 digits
   	 	// $ followed by any number of digits, decimal point, then two more digits
   	 	// Dont capture the $
   	 	String pattPriceCapture =  "\\$([0-9,]*\\.[0-9][0-9])";  
   	 	String pattQuantityCapture = "([0-9]*)";                // Any number of digits (though if more that 2 digits is unlikely)
   	 	String pattPSVCapture = "([0-9]*(?:\\.[0-9]*)?)";       // Could be 0, or a whole number, or a decimal value. So the decimal point and subsequent digits are optional 
        String pattDescCapture = "(.*)";                        // Anything 
   	 	// Optional /NN.NN PTS. Don't capture the "/" or the " PTS
        // May also be /NN PTS, ie the decimal point and places are optional
   	 	String pattPointsCapture = "(?:/(\\d*(?:\\.\\d*)?) PTS)?";           
        
   	 	String sPattern = pattSKUCapture
                    + " " + pattDescCapture 
                    + " " + pattQuantityCapture 
                    + " " + pattPriceCapture + pattPointsCapture
                    + " " + pattPSVCapture 
                    + " " + pattPriceCapture + pattPointsCapture;
   	 	
   	 	Pattern pattern= Pattern.compile(sPattern);
   	 	
   	 	Matcher m = pattern.matcher(line);
  	 	
   	 	if (m.lookingAt()) {
   	 		String SKU = m.group(1);
   	 		String description = m.group(2);
   	 		
   	 		// Need to remove commas from the price (if >$1000)
   	 		String sPrice = m.group(4).replaceAll(",", "");
   	 		
   	 		BigDecimal price = new BigDecimal(sPrice);
   	 		BigDecimal points = new BigDecimal(m.group(5) != null ? m.group(5):"0.00");
   	 		BigDecimal PSV = new BigDecimal(m.group(6));
   	 		p.quantity = Integer.parseInt(m.group(3)); 

   	 		if (p.quantity > 1) {
	   	 		// There appears to be an inconsistency in the orders. If more than one of an item is ordered,
	   	 		// for money, the unit price is .. the unit price, and the total price is unit price * quantity
	   	 		// If the products are purchased with points, the unit (points) price is the same as the total (points) price
	  	 		points = points.divide(new BigDecimal(p.quantity));
   	 		}
   	 		
   	 		// So ... if bought with points, the PSV is the PTS field.
   	 		// If bought with cash, the PSV is the PSV
   	 		// Note that some products have no PSV
   	 		if (points.compareTo(BigDecimal.ZERO) != 0) {
   	 			PSV = points;
   	 		}
   	 		
   	 		p.product = new Product(SKU, description, price, points, PSV);

   	 		
   	 	}
   	 	
   	 	return p;
		
	}
	
	
	// Difference from the text copied from the clipboard is that some things that are one line in the clipboard may
	// be several lines here.
	// Eg the order items appears in a table format, where each item is one row in a table. In the clipboard, one row is one line of text  
	// However in the PDF text, if the description spans multiple lines (in its table cell) then the text splits at that point.
	// So we have to reconstruct the entire table row
	public Order parse(String filename) {
		
		File file = new File(filename);
		FileInputStream stream;
		try {
			stream = new FileInputStream(file);
			return parse(stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public Order parse(InputStream stream) {

        String text = readPdfText(stream);
        
	    Order order = new Order();
	    
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
	    	
       	
	    	String line = reader.readLine();
	    	
	    	while(line != null) {

	    		line = line.trim();
	    		
	    		if (line.startsWith("Order Number" )) {
	    			order.orderNumber = line.substring(line.indexOf(':') + 2);
	    		}
	    		
	    		if (line.startsWith("Order Date")) {
	    			order.date = line.substring(line.indexOf(':') + 2);
                    order.date = Util.checkDateFormat(order.date);
	    		}
	    		else if (line.startsWith("ID:")) {
	    			// ID is on the next line
	    			order.account = reader.readLine().trim();
	    		}
	    		//Subtotal 	$358.25
	    		else if (line.startsWith("Subtotal")) {
	    			order.subtotal = parseDollars(line);
	    		}
	    		//Shipping 	$0.00
	    		else if (line.startsWith("Shipping") && line.contains("$")) {
    				order.shipping = parseDollars(line);
	    		}
	    		//Shipping address
	    		else if (line.startsWith("Shipping Address")) {
	    			// Address starts on next line and spans multiple lines. Read until "Shipping Method"
	    			order.shippingAddress = reader.readLine().trim();
	    			String s = reader.readLine().trim();
	    			while ( !s.equals("Shipping Method")) {
		    			order.shippingAddress += " " + s;
		    			s = reader.readLine().trim();
	    			}
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

	    			// Check that we added everything up properly
	    			// TODO need a generic way to report a parsing error to the user. Throwing a runtime exception
	    			// isn't my best work. On the other hand - this really shouldn't be possible
	    			if (order.getTotal().compareTo(parseDollars(line)) != 0) {
	    				throw new RuntimeException();
	    			}
	    		}
	    		else {
	    			
	    			// May be the start of an item 
	    			if (startsWithAnSKU(line)) {
	    				System.out.println(line);
	    				// If this is a complete item then it ends with quantity, unit price, PSV and total price  
	    				// Eg
	    				// 02110308 Enhancer II
	    				// (CA)
	    				// 1 $14.50 9.5 $14.50
	    				// OR quantity, unit price/points, PSV, total price/points
	    				// eg 
	    				// 02110826 Epoch
	    				// Firewalker Foot
	    				// Cream 100ml
	    				// (CA)
	    				// 1 $0.00/9.5
	    				// PTS
	    				// 0 $0.00/9.5
	    				// PTS

	    				// 02110308 Enhancer II (CA) 1 $14.50 9.5 $14.50
	    				// 02110826 Epoch Firewalker Foot Cream 100ml (CA) 1 $0.00/9.5 PTS 0 $0.00/9.5 PTS
	    				// But we need to delimite the fields with TABs 
	    				String fullLine = line;
	    				ProductAndQuantity p = isFullItemDescription(fullLine);
	    				
	    				while ( p.quantity == 0) {
	    					fullLine += " ";
	    					String partLine = reader.readLine();
	    					// Quit if we reached the end of the text
	    					if (partLine == null) break;
	    					fullLine += partLine;
	    					p = isFullItemDescription(fullLine);
	    				}
	    			
		    			if (p.quantity > 0) {
		    				order.addProduct(p.product, p.quantity);
		    			}
	    			}
	    			
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

}
