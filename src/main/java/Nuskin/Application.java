package Nuskin;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling           // Run database backup every day
public class Application implements CommandLineRunner {
    
	@Autowired
    ProductDatabase db;
	
    public static void main(String[] args) {
    	SpringApplication.run(Application.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
		
		boolean populateDatabase = false;
		boolean backupDatabase = true;
		
		if (backupDatabase) {
			// Might make sense to backup every time we start as well as the scheduled backup every day at midnight
			db.backup();
		}
		
		
		if (populateDatabase) {
		
			PriceListParser p = new PriceListParser();
			ArrayList<ProductType> products = p.parse(FileRoot.getRoot() + "PriceList/priceList.txt");
	
	        // Some specials that I don't know how to find 
	        ProductType special = new ProductType("02010721", "Pink Lumispa Normal/Combo", new BigDecimal("280.00"), new BigDecimal("248.00"), new BigDecimal("125.00"), new BigDecimal("156.25")) ;
	        products.add(special);
	        special = new ProductType("02010763", "Pink LumiSpa Valentine's Day Upgrade", new BigDecimal("324.00"), new BigDecimal("280.00"), new BigDecimal("135.00"), new BigDecimal("0")) ;
	        products.add(special);
	        // I suspect the price list has the wrong SKU for the LumiSpa gentle head - normal and firm are XXXX13 and 15, and on the website there is a gentle head xxxx14, but
	        // the price list has is as xxxx18
	        special = new ProductType("02310014", "ageLOC® LumiSpa Treatment Head Gentle", new BigDecimal("48.00"), new BigDecimal("42.00"), new BigDecimal("26.0"), new BigDecimal("35.0")) ;
	        products.add(special);
	        special = new ProductType("02001299", "ageLOC® Tru Face Essence Ultra Buy 3 Get 1 Free", new BigDecimal("712.00"), new BigDecimal("598.00"), new BigDecimal("409.0"), new BigDecimal("0")) ;
	        products.add(special);

	        special = new ProductType("02010774", "ageLOC LumiSpa Accent Preview Kit", new BigDecimal("245.00"), new BigDecimal("196.00"), new BigDecimal("134.0"), new BigDecimal("0")) ;
	        products.add(special);

	        
			WebpageParser webParser = new WebpageParser();
			webParser.parse(FileRoot.getRoot() + "PriceList/GalvanicSpa.txt");
			webParser.parse(FileRoot.getRoot() + "PriceList/GalvanicSpa_retail.txt");
			webParser.parse(FileRoot.getRoot() + "PriceList/ADR.txt");
			webParser.parse(FileRoot.getRoot() + "PriceList/ADR_retail.txt");
			webParser.parse(FileRoot.getRoot() + "PriceList/Pharmanex_ADR.txt");
			webParser.parse(FileRoot.getRoot() + "PriceList/Pharmanex_ADR_retail.txt");
			webParser.parse(FileRoot.getRoot() + "PriceList/LumiSpa.txt");
			webParser.parse(FileRoot.getRoot() + "PriceList/LumiSpa_retail.txt");
			
			webParser.parse(FileRoot.getRoot() + "PriceList/LumiSpa2.txt");
			webParser.parse(FileRoot.getRoot() + "PriceList/LumiSpa2_retail.txt");

			
	        ArrayList<ProductType>	webpageProducts = webParser.getProducts();
	        ArrayList<ProductType> uniqueProducts = new ArrayList<ProductType>();
	        
	        for (ProductType w: webpageProducts) {
			
	        	boolean duplicate = false;
	        	
				for (ProductType product: products) {
		
					if (product.getSku().equals(w.getSku())) {
	
						System.out.println("Duplicate product from webpage");
						duplicate = true;
						break;
						
					}
					
				}
				
				if (!duplicate) { 
					uniqueProducts.add(w);
				}
	        }
	        products.addAll(uniqueProducts);
			
			System.out.println(products.size() + " product types");
			
			for (ProductType productType : products) { 
				db.addProductType(productType);
			}
	
			//OrderParser orderParser = new OrderParser();
			//orderParser.parseAll();
		
		}
	}
}
