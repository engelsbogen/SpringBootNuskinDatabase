package Nuskin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class testJsonSerialize {

	@Test
	public void test() {

		Order order = new Order();
		
		order.setOrderNumber("123456");
		order.setAccount("CA9876543");
		
		Product product = new Product();
		
		product.setOrder(order);
		
		product.setCostPoints(new BigDecimal("12.34"));
		product.setDescription("Product description");
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			String json = mapper.writeValueAsString(product);
			assertThat(json, containsString("\"orderNumber\":\"123456\""));
			assertThat(json, containsString("\"costPoints\":12.34"));
			assertThat(json, containsString("\"description\":\"Product description\""));
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			fail("Serialization failure");
			
		}
		
	}

}
