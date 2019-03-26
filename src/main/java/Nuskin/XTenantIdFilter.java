package Nuskin;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@Order(1)  
public class XTenantIdFilter implements Filter {
 
    @Override
    public void doFilter(  ServletRequest req,   ServletResponse response,  FilterChain chain) throws IOException, ServletException {
  
        HttpServletRequest request = (HttpServletRequest) req;
        
        // Extract X-TenantID header
        Enumeration<String> headers = request.getHeaders("X-TenantID");
        
        // If it exists set the 
        if (headers.hasMoreElements()) {
        	String tenantName = headers.nextElement();
        	TenantContext.setTenantName(tenantName);
        }
  
        chain.doFilter(request, response);
    }

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}
 
}