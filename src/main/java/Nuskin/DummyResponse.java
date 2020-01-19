package Nuskin;

// Return an instance of this instead of ResponseEntity.ok() which has no content and no Content-Type header,
// which gets interpreted as an XML response in Firefox, and I get an error message in the console window.
// It seems this is a Firefox feature, Chrome is OK (googling confirms)
// Everything works fine apart from the error message, but if I return an object instead, it becomes a json
// content type and there are no errors in the console.
public class DummyResponse {

	int id = 0;
	
	public void setId(int id) {
	    this.id = id;
	}

	public int getId() {
		return id;
	}
}