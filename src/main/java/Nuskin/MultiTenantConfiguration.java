package Nuskin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    @ConfigurationProperties(
        prefix = "spring.datasource"
    )
  

    public DataSource dataSource() {
    	
    	
    	File[] files = getPropertiesFiles();
/*    	
        System.out.println("11111111111111$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    	ClassLoader classLoader = this.getClass().getClassLoader();
        System.out.println("3333333333333$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    	URL url = classLoader.getResource("tenants");
        System.out.println("5555555555555$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    	if (url == null ) {
    		System.out.println("Resource does not exist");
    		throw new RuntimeException("Resource does not exist");
    	}
        System.out.println(url + "      $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    
        String pathname = url.getFile();
        System.out.println(pathname + "  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    	File file = new File(pathname);
    	
    	//File file = new File(classLoader.getResource("tenants").getFile());
        files = file.listFiles();
        
        if (files == null) {
            System.out.println( " files is null$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                   	
        }
        else {
            System.out.println(files.length + "   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        }
        System.out.println(pathname + " contains " + files.length + " files");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        
        if (files.length == 0) {
        	
        	//System.err.println(pathname + " does not contain any datasource .properties files");
        	throw new RuntimeException(pathname + " does not contain any datasource .properties files");
        }
        */
    	
        //File[] files = Paths.get("tenants").toFile().listFiles();
        Map<Object,Object> resolvedDataSources = new HashMap<>();

        for(File propertyFile : files) {
            Properties tenantProperties = new Properties();
            DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader());

            try {
                tenantProperties.load(new FileInputStream(propertyFile));

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
            } catch (IOException e) {
                // Ooops, tenant could not be loaded. This is bad.
                // Stop the application!
                e.printStackTrace();
                return null;
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

    /**
     * Creates the default data source for the application
     * @return
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
    File[] getPropertiesFiles() {
    	
    	final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

    	File[] files = null;

    	String path = "tenants";
    	
    	if(jarFile.isFile()) {  // Run with JAR file
    	    JarFile jar;
			try {
				jar = new JarFile(jarFile);
	    	    final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	    	    
	    	    ArrayList<File> af = new ArrayList<File>();
	    	    
	    	    while(entries.hasMoreElements()) {
	    	        final String name = entries.nextElement().getName();
	    	        if (name.startsWith(path + "/")) { //filter according to the path
	    	            af.add(new File(name));
	    	        }
	    	    }
	    	    
	    	    files = (File[]) af.toArray();
	    	    
	    	    jar.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else { // Run with IDE
    	    final URL url = this.getClass().getClassLoader().getResource(path);
    	    if (url != null) {
    	        try {
    	            final File folder = new File(url.toURI());
    	            files = folder.listFiles();
    	        } catch (URISyntaxException ex) {
    	            // never happens
    	        }
    	    }
    	}    	
    	
    	return files;
    	
    }
    
    
}