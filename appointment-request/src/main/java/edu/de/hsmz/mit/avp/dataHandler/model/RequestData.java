package edu.de.hsmz.mit.avp.dataHandler.model;

public class RequestData {

	private REQUESTACTIONENUM action;
	private REQUESTTYPEENUM typ;
	
	public RequestData(REQUESTACTIONENUM action, REQUESTTYPEENUM typ) {
		super();
		this.action = action;
		this.typ = typ;
	}
	public REQUESTACTIONENUM getAction() {
		return action;
	}
	public REQUESTTYPEENUM getTyp() {
		return typ;
	}
}
