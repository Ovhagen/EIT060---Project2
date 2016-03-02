package Exeptions;

public class WrongFormatException extends Exception{
	
	public WrongFormatException() {
		super();
	}

	public WrongFormatException(String message) {
		super(message);
	}

	public WrongFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongFormatException(Throwable cause) {
		super(cause);
	}

}
