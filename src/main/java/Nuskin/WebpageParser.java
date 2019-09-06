package Nuskin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;

public class WebpageParser {


	ArrayList<ProductType> products = new ArrayList<ProductType>();
	
	public ArrayList<ProductType> getProducts() {
		return products;
	}

	ProductType findProduct(BufferedReader reader) {

		ProductType product = null;
		boolean isRetail = false;
		
		try {
	    	String line = reader.readLine();
	    	
	    	// Search for "Quick View"
	    	while(line != null && !line.equals("Quick View")) {
	    		line = reader.readLine();
	    	}
	    	
	    	if (line != null) {
		    	BigDecimal psv = new BigDecimal(0);
		    	String sku = null;
		    	String description = reader.readLine();
		    	String price = reader.readLine();
		    	reader.readLine(); // empty line
		    	
		    	// Retail webpage doesnt have the PSV
		    	String spsv = reader.readLine().trim();

		    	if (spsv.startsWith("PSV") ) {
		    		
		    		// There is one product with PSV = ---
			    	if (!spsv.equals("PSV ---")) {
			    		psv = new BigDecimal(spsv.substring(4));
			    	}
			    	
			    	sku = reader.readLine().trim();
			    	isRetail = false;
		    	}
		    	else {
		    		// No PSV so this is a retail view and the string we have is the SKU
		    		sku = spsv;
		    		isRetail = true;
		    	}

		    	if (isRetail) {
		    		// Price is the retail price
			    	product = new ProductType(sku, description,  new BigDecimal(price.substring(1)), new BigDecimal(0), psv, new BigDecimal(0));
		    		
		    	}
		    	else {
		    		product = new ProductType(sku, description, new BigDecimal(0), new BigDecimal(price.substring(1)), psv, new BigDecimal(0));
		    	}
	    	}
		}
		catch(IOException e) {
			// End of file, just return null
		}
    	return product;
		
	}


	void storeProduct(ProductType product) {
		
		if (product != null) {
			boolean isDuplicate = false;
			// Check if this is a duplicate, and if so merge retail and wholesale prices, psv, csv
			
			for (ProductType p : products) {
				
				if (p.getSku().equals(product.getSku())) {
					
					isDuplicate = true;
					
					if (p.getRetailPrice().compareTo(BigDecimal.ZERO) == 0) {
						p.setRetailPrice(product.getRetailPrice());
					}
					if (p.getWholesalePrice().compareTo(BigDecimal.ZERO) == 0) {
						p.setWholesalePrice(product.getWholesalePrice());
					}
					if (p.getPsv().compareTo(BigDecimal.ZERO) == 0) {
						p.setPsv(product.getPsv());
					}
					if (p.getPsv().compareTo(BigDecimal.ZERO) == 0) {
						p.setCsv(product.getCsv());
					}
				}
			}
			
			if (!isDuplicate) {
				products.add(product);
			}
		}
	}
	
	
	public void parse(String filename) {
		
	    BufferedReader reader;
	    
        try {
	    	
	    	//reader = new BufferedReader(new FileReader(filename));
	    	reader = new BufferedReader( new InputStreamReader(new FileInputStream(filename), "UTF-8"));

	    	ProductType product;

            do {
            	product = findProduct(reader);
            	
            	if (product != null) {
            		storeProduct(product);
            	}
            }
            while (product != null);

	    	reader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	protected void show(ArrayList<ProductType> products) {
		
		for (ProductType product: products) {
			
			product.show();
		}
		
	}
	
	
	
}
