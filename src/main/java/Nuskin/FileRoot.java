package Nuskin;

import java.io.File;

public class FileRoot {

	
	public static String getSecondaryPath() {
		
		return "/media/WDMyBook/Nuskin/";
		
	}
	
	public static String getRoot() {

		
		String[] locations = { 
			"/home/adc/eclipse-angular/SpringBootNuskinDatabase/TextFiles/",
			"/home/adc/nuskin-order-manager/"	
		};
		
		for (String s: locations) {

			File path = new File(s);
			
			if (path.exists()) {
				return s;
			}
		}

		return "/";
		
	}
	
}
