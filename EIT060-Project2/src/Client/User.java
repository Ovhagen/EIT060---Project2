package Client;

public abstract class User {
	protected String personalID, name;
	
	public User(String personalID, String name){
		this.personalID = personalID;
		this.name = name;
	}

	public String getID(){
		return personalID;
	}
	
	public String getName(){
		return name;
	}
	
	
	
}
