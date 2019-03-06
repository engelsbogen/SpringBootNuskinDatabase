package Nuskin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;



public class OrderParserPDF extends OrderParser {

	
	public static void main(String[] args ) {

	    	
		new OrderParserPDF().parse("/home/adc/eclipse-angular/SpringBootNuskinDatabase/TextFiles/Orders/0235048533.pdf");
		
	}

	
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

        /*
		try (PrintWriter out = new PrintWriter(filename + ".txt")) {
			out.println(text);
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
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
   	 	String pattPriceCapture =  "\\$([0-9]*\\.[0-9][0-9])";  
   	 	String pattQuantityCapture = "([0-9]*)";                // Any number of digits (though if more that 2 digits is unlikely)
   	 	String pattPSVCapture = "([0-9]*(?:\\.[0-9]*)?)";       // Could be 0, or a whole number, or a decimal value. So the decimal point and subsequent digits are optional 
        String pattDescCapture = "(.*)";                        // Anything 
   	 	// Optional /NN.NN PTS. Don't capture the "/" or the " PTS
   	 	String pattPointsCapture = "(?:/(\\d*(?:\\.\\d*)) PTS)?";           
        
   	 	Pattern pattern= Pattern.compile(pattSKUCapture
   	 			                         + " " + pattDescCapture 
   	 			                         + " " + pattQuantityCapture 
   	 			                         + " " + pattPriceCapture + pattPointsCapture
   	 			                         + " " + pattPSVCapture 
   	 			                         + " " + pattPriceCapture + pattPointsCapture);
   	 	
   	 	Matcher m = pattern.matcher(line);
  	 	
   	 	if (m.lookingAt()) {
   	 		String SKU = m.group(1);
   	 		String description = m.group(2);
   	 		
   	 		BigDecimal price = new BigDecimal(m.group(4));
   	 		BigDecimal points = new BigDecimal(m.group(5) != null ? m.group(5):"0.00");
   	 		BigDecimal PSV = new BigDecimal(m.group(6));
   	 		p.product = new Product(SKU, description, price, points, PSV);

   	 		p.quantity = Integer.parseInt(m.group(3)); 
   	 	}
   	 	
   	 	return p;
		
	}
	
	
	// Difference from the text copied from the clipboard is that some things that are one line in the clipboard may
	// be several lines here.
	// Eg the order items appears in a table format, where each item is one row in a table. In the clipboard, one row is one line of text  
	// However in the PDF text, if the description spans multiple lines (in its table cell) then the text splits at that point.
	// So we have to reconstruct the entire table row
	public void parse(String filename) {
		
		File file = new File(filename);
		FileInputStream stream;
		try {
			stream = new FileInputStream(file);
			parse(stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
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
	    			if (order.getTotal().compareTo(parseDollars(line)) != 0) {
	    				throw new RuntimeException();
	    			}
	    		}
	    		else {
	    			
	    			// May be the start of an item 
	    			if (startsWithAnSKU(line)) {
	    				
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
	    			
		    			if (p.quantity > 0) order.addProduct(p.product, p.quantity);
	    			}
	    			
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

	    return order;

	}

}
