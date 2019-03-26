package Nuskin;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantAwareRoutingSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
    	
    	// Tenant name supplied in X-TenantID HTTP header is used to lookup which
    	// database to use. This should be the same as the 'name' property in 
    	// the tenants/xxx.properties file
        return TenantContext.getTenantName();
    }

}