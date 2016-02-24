package User;

public abstract class Employee extends User{
	protected int division;
	
	public Employee(String personalID, String name, int division){
		super(personalID, name);
		this.division = division;
	}

	public int getDivisionID(){
		return division;
	}
	
}
