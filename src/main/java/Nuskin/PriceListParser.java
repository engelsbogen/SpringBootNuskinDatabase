package Nuskin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class PriceListParser {

	ArrayList<ProductType> products = new ArrayList<ProductType>();
	
	ColourCosmeticsParser ccp = new ColourCosmeticsParser();
	
	ArrayList<ProductType> findProduct(BufferedReader reader) {

		ArrayList<ProductType> newProducts = new ArrayList<ProductType>();
		
		try {
	    	String line = reader.readLine();
	    	String sku = null;
	    	
	    	// Search for an SKU, format is 02 NNNNNN 
	    	// EXCEPT for a handful of cosmetic products that are available in different shades. These shades are not
	    	// itemised separately, and the SKU is marked as *
	    	while(line != null && sku == null) {
	
	    		line = line.trim();
	    		
	    		if (line.equals("*")) {
	    			sku = "*";
	    			break;
	    		}
	    		else if (line.matches("02 [0-9]{6}")) {
	    			// Remove the space from the SKU as orders don't have it
	    			sku = line.substring(0,2) + line.substring(3,9);
	    			break;
	    		}
	    		line = reader.readLine();
	    	}
	    	
	    	if (sku != null) {
	    		String description = reader.readLine().trim();
	    		
	    		// Some descriptions are multi-line, seems to occur after a superscript but not always.
	    		// Try the next line until it looks like a price 
	    		// first idea was it starts with at least two digits (no prices are less than $10 !) then optional decimal point and more digits
	    		// Gaagh there is a product less than $10 - lip balm at $9.75
	    		// One of the extra lines is a "2" which could be a valid price of $2 except there are nothing priced at that
	    		// Going to treat the lip balm as a special case
	    		String s = reader.readLine();
	    		
	    		while( !s.matches("[1-9][0-9][.0-9]*") && !s.matches("[1-9].[0-9]{2}")) {
	    			description += s;
	    			s = reader.readLine();
	    		}
	    		
	    		BigDecimal retailPrice = new BigDecimal(s);
	    		BigDecimal wholesalePrice = new BigDecimal(reader.readLine());
	    		BigDecimal psv = new BigDecimal(reader.readLine());
	    		BigDecimal csv = new BigDecimal(reader.readLine());

	    		
		    	if (sku.equals("*")) {
		    		ArrayList<ProductType> colourCosmetics = resolveSKU(description);
		    		
		    		// Update each product with the full data (only partial data available from webpage)
		    		for (ProductType colourCosmetic : colourCosmetics ) {
		    			colourCosmetic.retailPrice = retailPrice;
		    			colourCosmetic.wholesalePrice = wholesalePrice;
		    			colourCosmetic.psv = psv;
		    			colourCosmetic.csv = csv;
		    		}
		    		
		    		newProducts = colourCosmetics;
		    	}
		    	else {
		    		ProductType product = new ProductType(sku, description, retailPrice, wholesalePrice, psv, csv);
		    		newProducts.add(product);
		    	}
	    	}
		}
		catch(IOException e) {
			// End of file, just return null
		}
    	return newProducts;
		
	}
	
	
	ArrayList<ProductType> resolveSKU(String description) {
		
		// Products in the price list that come in several colours are shown with SKU="*"
		// I have to work them out separately
		return ccp.getProductList(description);
	}
	

	void show() {
		
		for (ProductType product : products ) {
			product.show();
		}
		
	}
	
	ArrayList<ProductType> parse(String filename) {
		
		// First parse the cosmetics so we can add all the different colours
		ccp.parseAll();
		
	    BufferedReader reader;
	    
        try {
	    	
	    	reader = new BufferedReader(new FileReader(filename));
	    	
	    	ArrayList<ProductType> newProducts;
	    	
            do {
            	newProducts = findProduct(reader);
            	products.addAll(newProducts);
            }
            while (!newProducts.isEmpty());
            
	
	    	reader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
        
		return products;
	}
	

	public static void main(String[] args) {
		
		PriceListParser p = new PriceListParser();
		p.parse(FileRoot.getRoot() + "PriceList/priceList.txt");
		p.show();
		
		
	}
	
}
