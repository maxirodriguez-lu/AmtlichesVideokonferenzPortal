package edu.de.hsmz.mit.avp.dataHandler.model;

public enum STATUSENUM {

	OKAY ("Okay"),
	NA ("na"),
	ERROR("Error");
	
	private String text;
	
	STATUSENUM(String text){
		this.text = text;
	}
	
	public String getText(){
		return this.text;
	}
	
	public static STATUSENUM fromString(String text){
		if(text != null){
			for(STATUSENUM rt: STATUSENUM.values()){
				if(text.equalsIgnoreCase(rt.text)) return rt;
			}
		}
		return null;
	}
}
