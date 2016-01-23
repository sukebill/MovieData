package dataTypes;

public class Actor {

	private String name;
	private String wiki;
	
	public Actor(String name, String wiki){
		this.name = name;
		this.wiki = wiki;
	}
	
	public Actor(){
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getWiki() {
		return wiki;
	}
	public void setWiki(String wiki) {
		this.wiki = wiki;
	}
}
