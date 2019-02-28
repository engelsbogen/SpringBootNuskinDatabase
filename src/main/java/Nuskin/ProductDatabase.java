package Nuskin;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProductDatabase {

	private Logger  log =  LoggerFactory.getLogger(ProductDatabase.class);
	
	private static ProductDatabase theProductDatabase = null;
	
	@Autowired
    private  ProductRepository productRepository;
	@Autowired 
    private  ProductTypeRepository productTypeRepository;
	@Autowired 
    private  OrderRepository orderRepository;

	public static ProductDatabase getDB() { return theProductDatabase; }
	
	
	ProductDatabase() {
		// Bodge to make singleton 
		theProductDatabase = this;
	}
	
	void addProductType(ProductType productType) {
		productTypeRepository.save(productType);
	}
	
	void addProduct(Product product) {
		productRepository.save(product);
	}
	
	void addOrder(Order order) {
		orderRepository.save(order);
	}
	
	Order getOrder(String orderNumber) {
		Optional<Order> optOrder = orderRepository.findById(orderNumber);
		
		Order order = null;
		if (optOrder.isPresent()) {
			order = optOrder.get();
			order.getProductsFromDatabase();
		}
		
		return order;
	}
	
	ProductType findProductType(String SKU) {
		Optional<ProductType> productType = productTypeRepository.findBySku(SKU);
		
		if (!productType.isPresent()) {
			System.err.println("Product " + SKU + " not found in database");
		}
		
		return productType.get();
	}
	
	ArrayList<Product> getOrderItems(String orderNumber) { 
		return productRepository.findAllByOrderOrderNumber(orderNumber);
	}
	
    @Scheduled(cron = "0 0 0 * * *")  // Backup at midnight every day
	public boolean backup() {
		
		String user="adc";
		String password="letmein";
		String database="Nuskin";
		String backupPath = FileRoot.getRoot() + "Backups";
		
		return doBackup(user,password,database,backupPath);
	}
		
	private boolean doBackup(String user, String password, String database, String backupPath) {
		
        boolean status = false;
        
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date date = new Date();
            String backupFilename = "backup-" + database + "-" + dateFormat.format(date) + ".sql";
            
            ProcessBuilder pb = new ProcessBuilder("mysqldump", 
            		                               "-u", user,
            		                               "--password=" + password,
            		                               "--add-drop-database",
            		                               "-B",
            		                               database,
            		                               "-r", backupPath + "/" + backupFilename);
            
            Process p = pb.start();
          
            int processStatus = p.waitFor();
 
            if (processStatus == 0) {
                status = true;
                log.info("Backup created successfully for " + database );
            } else {
                status = false;
                log.info("Could not create the backup for " + database );
            }
            
 
        } catch (IOException ioe) {
            log.error("IOException:", ioe, ioe.getCause());
        } catch (Exception e) {
            log.error("Exception:", e, e.getCause());
        }
        return status;
	}
	
	
	
	
		
	public boolean restore(String dbUserName, String dbPassword, String source) {
	 
		// Source SQL text contains the database name so. Maybe should check this is Nuskin before doing anything?
        ProcessBuilder pb = new ProcessBuilder("mysql", "--user=" + dbUserName, "--password=" + dbPassword, "-e", "source " + source);
 
        Process runtimeProcess;
        try {
            runtimeProcess = pb.start();
            int processComplete = runtimeProcess.waitFor();
 
            if (processComplete == 0) {
                log.info("Backup restored successfully with " + source);
                return true;
            } else {
                log.info("Could not restore the backup " + source);
            }
        } catch (Exception ex) {
            log.error("Exception: " + ex, ex.getCause());
        }
 
        return false;
 
    }
}
