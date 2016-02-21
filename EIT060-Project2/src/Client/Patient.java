package Client;

public class Patient extends User{
	
	/**PersonalID for patient is the same as his/her social security number*/
	public Patient(String personalID, String name){
		super(personalID, name);
	}


}
