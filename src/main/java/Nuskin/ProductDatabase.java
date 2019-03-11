package Nuskin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
		
		ProductType existing = findProductType(productType.getSku());
		
		if (existing == null) {
			productTypeRepository.save(productType);
		}
		else {
			// TODO may try to merge or update
			
		}
		
		
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
			return null;
		}
		else {
			return productType.get();
		}
		
	}
	
	ArrayList<Product> getOrderItems(String orderNumber) { 
		
		ArrayList<Product> items = new ArrayList<Product>();
		try {
			items = productRepository.findAllByOrderOrderNumber(orderNumber);
		}
		catch(javax.persistence.EntityNotFoundException e) {
			System.out.println(e.toString());
		}
		
		return items;
	}
	
    @Scheduled(cron = "0 0 0 * * *")  // Backup at midnight every day
	public boolean backup() {
		
		String user="adc";
		String password="letmein";
		String database="Nuskin";
		String backupPath = FileRoot.getRoot() + "Backups";
		
		String secondaryBackupPath = FileRoot.getSecondaryPath() + "/Backups";
		
		
		return doBackup(user,password,database,backupPath, secondaryBackupPath);
	}
	
    
    /* This was work in progress to talk to Microsoft onedrive 
     * TDB
    private String getAccessToken() {
    	
    	String appID="4c2154bd-f9a3-40e7-a0bc-2f6426e43c14";
    	RestTemplate rt = new RestTemplate();
    	
    	String accessToken = null;
    	
    	HttpHeaders headers = new HttpHeaders();
	   	headers.add("Content-Type", "application/x-www-form-urlencoded");
    	headers.add("Accept","application/json");
    	
    	String requestBody="client_id=" + appID;
    	
    	HttpEntity<String> entity = new HttpEntity<String>(requestBody, headers); 
    	String url = "https://login.microsoftonline.com/token";
    	
    	rt.put(url, entity);
    	
    	return "XYZ";
    	
    }
    
    
    public void uploadToOneDrive() {
    	
    	// App ID/Client ID:  4c2154bd-f9a3-40e7-a0bc-2f6426e43c14
    	
    	//PUT /me/drive/root:/FolderA/FileB.txt:/content
    	//Content-Type: text/plain
    	//The contents of the file goes here.
    	
    	String accessToken = getAccessToken();
    	
    	String filename = "backup-Nuskin-28-02-2019.sql";
    	
    	String requestBody = null;  // The text from the .SQL file?
    	
    	try {
			byte[] b = Files.readAllBytes(Paths.get(FileRoot.getRoot() + "/Backups/" + filename));
			requestBody=new String(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	RestTemplate rt = new RestTemplate();
    	
 	
    	HttpHeaders headers = new HttpHeaders();
    	headers.add("Authorization", "Bearer " + accessToken);
	   	headers.add("Content-Type", "text/plain");
    	headers.add("Accept","application/json");
    	
   	
    	HttpEntity<String> entity = new HttpEntity<String>(requestBody, headers); 
    	String url = "https://graph.microsoft.com/v1.0/me/drive/root:/Dokumente/" + filename + ":/content";
    	
    	rt.put(url, entity);
    	
    }
    */
    
	private boolean doBackup(String user, String password, String database, String backupPath, String secondaryBackupPath) {
		
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
                
                // Copy backup file to external locations.
                // (1) USB disk
                Files.copy(Paths.get(backupPath+"/"+backupFilename), Paths.get(secondaryBackupPath + "/" + backupFilename));

                // (2) The cloud
                GoogleDrive.upload(backupFilename);
                                
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
