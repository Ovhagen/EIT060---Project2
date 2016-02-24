package User;

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
	
	@Override
	public String toString(){
		return personalID;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((personalID == null) ? 0 : personalID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (personalID == null) {
			if (other.personalID != null)
				return false;
		} else if (!personalID.equals(other.personalID))
			return false;
		return true;
	}

	
}
