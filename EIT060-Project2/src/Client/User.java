package Client;

public abstract class User {
	protected String personalID;
	
	public User(String personalID){
		this.personalID = personalID;
	}

	public String getID(){
		return personalID;
	}
	
	
	
}
