package Client;

public abstract class Employee extends User{
	protected int division;
	
	public Employee(int personalID, int division){
		super(personalID);
		this.division = division;
	}

	public int getDivisionID(){
		return division;
	}
	
}
