package Client;

public abstract class User {
	protected int personalID;
	
	public User(int personalID){
		this.personalID = personalID;
	}

	public int getID(){
		return personalID;
	}
	
	
}
