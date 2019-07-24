package Nuskin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;

public class testPdfParser {

	@Test
	public void test() {
		
		
		File file = new File("/home/adc/eclipse-angular/SpringBootNuskinDatabase/TextFiles/Orders/tabloid.pdf");
		FileInputStream stream = null;
		
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Retrieving text from PDF document
		String text = "";
		try {
			PDDocument document = PDDocument.load(stream);

			PDPage page = document.getPage(0);

			InputStream strm  = page.getContents();
			
			byte[] b = strm.readAllBytes();
			
			System.out.println(b);
			
			PDDocument pageDoc = PDDocument.load(b);
			

			
			
			// Instantiate PDFTextStripper class
			PDFTextStripper pdfStripper = new PDFTextStripper();
			text = pdfStripper.getText(pageDoc);

			document.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	    System.out.println( text);
	
		
	}

}
