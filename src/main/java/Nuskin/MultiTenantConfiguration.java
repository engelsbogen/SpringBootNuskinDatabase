package Nuskin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MultiTenantConfiguration {

    @Autowired
    private DataSourceProperties properties;

    /**
     * Defines the data source for the application
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
    	
    	List<File> files = getPropertiesFiles();
    	
        //File[] files = Paths.get("tenants").toFile().listFiles();
        Map<Object,Object> resolvedDataSources = new HashMap<>();
        
        if (files != null) {
	        for(File propertyFile : files) {
	        	
	            Properties tenantProperties = new Properties();
	            DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader());
	
	            try {
	                tenantProperties.load(getInputStream(propertyFile));
	
	                String tenantId = tenantProperties.getProperty("name");
	
	                // Assumption: The tenant database uses the same driver class
	                // as the default database that you configure.
	                dataSourceBuilder.driverClassName(properties.getDriverClassName())
	                                 .url(tenantProperties.getProperty("datasource.url"))
	                                 .username(tenantProperties.getProperty("datasource.username"))
	                                 .password(tenantProperties.getProperty("datasource.password"));
	
	                if(properties.getType() != null) {
	                    dataSourceBuilder.type(properties.getType());
	                }
	
	                resolvedDataSources.put(tenantId, dataSourceBuilder.build());
	            } 
	            catch (IOException e) {
	                // Ooops, tenant could not be loaded. This is bad.
	                // Stop the application!
	                e.printStackTrace();
	                return null;
	            }
	        }
        }
        // Create the final multi-tenant source.
        // It needs a default database to connect to.
        // Make sure that the default database is actually an empty tenant database.
        // Don't use that for a regular tenant if you want things to be safe!
        TenantAwareRoutingSource dataSource = new TenantAwareRoutingSource();
        dataSource.setDefaultTargetDataSource(defaultDataSource());
        dataSource.setTargetDataSources(resolvedDataSources);

        // Call this to finalize the initialization of the data source.
        dataSource.afterPropertiesSet();

        return dataSource;
    }
    
    
    InputStream getInputStream(File propertyFile) throws FileNotFoundException {
    	
    	if (propertyFile.isFile()) {
    		// A property file in the file system
    		return new FileInputStream(propertyFile);
    	}
    	else {
    		// Property file in jar, load as resource
    		return this.getClass().getClassLoader().getResourceAsStream(propertyFile.getPath());
    	}

    }
    
    /**
     * Creates the default data source for the application
     */
    private DataSource defaultDataSource() {
    	
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader())
                .driverClassName(properties.getDriverClassName())
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword());

        if(properties.getType() != null) {
            dataSourceBuilder.type(properties.getType());
        }

        return dataSourceBuilder.build();
    }
    
    
    
    // Apparently its not possible to get files directly from a jar, so we have to detect if we are 
    // running from a jar or from the IDE 
    private List<File> getPropertiesFiles() {
    	
    	final String classFileName = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    	    	
    	List<File> files = null;

    	String path = "tenants";
    	
   	 	Pattern pattern= Pattern.compile("file:(.*jar)!");
   	 	Matcher m = pattern.matcher(classFileName);
    	
    	if(m.lookingAt()) {  // Run with JAR file

    		String jarPath = m.group(1); 
        	JarFile jar;
			try {
				jar = new JarFile(new File(jarPath));
	    	    final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	    	    
	    	    files = new ArrayList<File>();
	    	    
	    	    while(entries.hasMoreElements()) {
	    	        final String name = entries.nextElement().getName();
	    	        if (name.endsWith(".properties") && name.contains(path + "/")) { 
	    	            files.add(new File(name));
	    	        }
	    	    }
	    	    
	    	    jar.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
    	} 
    	else { // Run with IDE
    	    final URL url = this.getClass().getClassLoader().getResource(path);
    	    if (url != null) {
    	        try {
    	            final File folder = new File(url.toURI());
    	            files = Arrays.asList(folder.listFiles());
    	            Arrays.asList(files);
    	        } 
    	        catch (URISyntaxException ex) {
    	            // never happens
    	        }
    	    }
    	}    	
    	
    	return files;
    	
    }
    
    
}