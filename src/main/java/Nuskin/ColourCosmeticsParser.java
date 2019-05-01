package Nuskin;

import java.util.ArrayList;
import java.util.HashMap;




public class ColourCosmeticsParser extends WebpageParser {

	
	// Lips
	ArrayList<ProductType> lipTints = new ArrayList<ProductType>();
	ArrayList<ProductType> lipsticks = new ArrayList<ProductType>();
	ArrayList<ProductType> replenishingLipsticks  = new ArrayList<ProductType>();	
	ArrayList<ProductType> contouringLipGlosses  = new ArrayList<ProductType>();	 
	ArrayList<ProductType> powerlipsFluids  = new ArrayList<ProductType>();

	// Cheeks
	ArrayList<ProductType> subtleEffectBlushes  = new ArrayList<ProductType>();	 
	ArrayList<ProductType> blushDuos  = new ArrayList<ProductType>();	 

	// Foundation
	ArrayList<ProductType> liquidFinishes  = new ArrayList<ProductType>();	 
	ArrayList<ProductType> concealers  = new ArrayList<ProductType>();	
	ArrayList<ProductType> tintedMoisturizers = new ArrayList<ProductType>();
	
	// Powders
	ArrayList<ProductType> pressedPowders = new ArrayList<ProductType>();
	
	// Eyes
	ArrayList<ProductType> eyeShadows  = new ArrayList<ProductType>();	 
	ArrayList<ProductType> eyeLiners  = new ArrayList<ProductType>();	
	
	// Map from price list generic name to list of products of that type
	HashMap<String, ArrayList<ProductType>> mapA = new HashMap<String, ArrayList<ProductType>>();
	
	ColourCosmeticsParser() {
	
		mapA.put("Lip Tint", lipTints);
		mapA.put("Lipstick", lipsticks );
		mapA.put("Replenishing Lipstick", replenishingLipsticks);	
		mapA.put("Contouring Lip Gloss", contouringLipGlosses);	 
		mapA.put("Powerlips Fluid", powerlipsFluids);
		// Cheeks
		mapA.put("Subtle Effects Blush", subtleEffectBlushes);	 
		mapA.put("Blush Duo", blushDuos);
		// Foundation
		mapA.put("Advanced Liquid Finish", liquidFinishes);	 
		mapA.put("Skin Beneficial Concealer", concealers);
		mapA.put("Advanced Tinted Moisturizer", tintedMoisturizers);
		// Powders
		mapA.put("MoisturShade Wet/Dry Pressed Powder", pressedPowders);
		// Eyes
		mapA.put("Desired Effects® Eye Shadow", eyeShadows);
		mapA.put("Defining Effects Smooth Eye Liner", eyeLiners);	
	}	
	

	ArrayList<ProductType> getProductList(String genericName) {
		
		ArrayList<ProductType> cosmeticList = mapA.get(genericName);
		
		if (cosmeticList == null) {
			System.err.println(genericName + " not mapped");
			throw new RuntimeException();
		}
		
		return cosmeticList;
		
	}

	boolean isPowerLipsColour(String description) {

		String[] powerLipsColours = { 
			"Confidence",
			"Persistence",
			"Determined",
			"Maven",
			"Explore",
			"Fearless",
			"Roar",
			"Ruler",
			"Reign",
			"Breadwinner",
			"Promotion",
			"Unbreakable"
		};
		
		for (String colour: powerLipsColours) {
			if (description.equals(colour)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
	
    // Called from the super class to save a product. Here we add it to one of the lists depending on
	// what it is (default superclass action stores in one list)
	void storeProduct(ProductType product) {
		
    	if (product != null) {
        	
    		// Now decide what class of product it is
        	
        	// Lips
    		if (product.description.endsWith("Lip Tint")) {
    			lipTints.add(product);
    		}
    		else if (product.description.endsWith("Lipstick")) {
    			// "plain" lipsticks are described as <Colour> Lipstick on the web page
    			lipsticks.add(product);
    		}
    		else if (product.description.startsWith("Lipstick")) {
    			// Replenishing lipsticks are described as "Lipstick - <colour>" on the web page
    			replenishingLipsticks.add(product);
    		}
    		else if (product.description.startsWith("Contouring Lip Gloss")) {
    			contouringLipGlosses.add(product);
    		}
    		else if (product.description.contains("Powerlips Fluid")) {
    			powerlipsFluids.add(product);
    		}
    		
    		// The original PowerLips colours don't identify themselves as PowerLips 
    		// All we see is the colour, these are:
    		else if (isPowerLipsColour(product.getDescription())) {
    			powerlipsFluids.add(product);
    			
    		}
    		
    		
    		
    		// Cheeks
    		else if (product.description.endsWith("Blush")) {
    			// This is compact with blusher and brush
    			blushDuos.add(product);
    		}
    		else if (product.description.startsWith("Blush")) {
    			subtleEffectBlushes.add(product);
    		}
    		
    		// Foundation
    		else if (product.description.startsWith("Concealer")) {
    			concealers.add(product);
    		}
    		else if (product.description.startsWith("Advanced Tinted Moisturizer")) {
    			tintedMoisturizers.add(product);
    		}
    		else if (product.description.startsWith("Advanced Liquid Finish")) {
    			liquidFinishes.add(product);
    		}
    		
    		// Powders
    		else if (product.description.contains("Pressed Powder")) {
    			pressedPowders.add(product);
    		}
    		
    		// Eyes
    		// For some reason the Amethyst shade eye shadow doesn't have Eye Shadow in its name
    		else if (product.description.startsWith("Eye Shadow") || product.description.equals("Amethyst")) {
    			eyeShadows.add(product);
    		}
    		else if (product.description.startsWith("Eye Liner")) {
    			eyeLiners.add(product);
    		}
    		else {
    			// There are some individual products on these pages that we dont need to worry about.
    		}
    	}		
	}

	
	void parseAll() {
		
		String [] files = {
				"Lips.txt",
				"Cheeks.txt",
				"Concealers.txt",
				"Moisturizers.txt",
				"LiquidFinish.txt",
				"PressedPowders.txt",
				"Eyes.txt"
		};
		
		String fileRoot = FileRoot.getRoot() + "PriceList/"; 
		
		for (String file : files ) {
			parse( fileRoot + file );
		}
	}
	
	
	public static void main(String[] args) {
		ColourCosmeticsParser p = new ColourCosmeticsParser();
		p.parseAll();
	}
	
}
