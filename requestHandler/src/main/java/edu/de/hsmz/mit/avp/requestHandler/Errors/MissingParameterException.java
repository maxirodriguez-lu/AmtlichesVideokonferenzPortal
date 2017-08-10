package edu.de.hsmz.mit.avp.requestHandler.Errors;

public class MissingParameterException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1101303014719146319L;
	String errorText;

	public MissingParameterException(String errorText) {
		this.errorText = errorText;
	}
	
	@Override
	public String getMessage(){
		return this.errorText;
	}

}
