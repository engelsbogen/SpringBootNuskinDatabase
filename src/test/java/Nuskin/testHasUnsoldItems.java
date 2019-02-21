package Nuskin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class testHasUnsoldItems {

	@Test
	public void test() {
		
		Order order = new Order();
		
		order.setOrderNumber("123456");
		order.setAccount("CA9876543");
		
		Product product = new Product();
		product.setCostPoints(new BigDecimal("12.34"));
		product.setDescription("Product description");
		product.setOrder(order);
		product.setEndUse(Product.EndUse.INSTOCK);

		order.addProduct(product, 1);
		
		Product product2 = new Product();
		product2.setCostPoints(new BigDecimal("12.34"));
		product2.setDescription("Product description");
		product2.setOrder(order);
		product2.setEndUse(Product.EndUse.DEMO);
		
		assertTrue(order.hasUnsoldItems());
	
		ObjectMapper mapper = new ObjectMapper();

		try {
			String json = mapper.writeValueAsString(order);
			assertThat(json, containsString("\"hasUnsoldItems\":true"));
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Serialization failure");
		}

		
		
		
		product.setEndUse(Product.EndUse.SAMPLE);
		assertFalse(order.hasUnsoldItems());
		try {
			String json = mapper.writeValueAsString(order);
			assertThat(json, containsString("\"hasUnsoldItems\":false"));
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Serialization failure");
		}
		
	}

}
