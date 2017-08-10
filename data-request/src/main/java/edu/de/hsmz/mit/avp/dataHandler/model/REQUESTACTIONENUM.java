package edu.de.hsmz.mit.avp.dataHandler.model;

public enum REQUESTACTIONENUM {
	READ ("Read"),
	ADD ("Add"),
	UPDATE ("Update"),
	DELETE ("Delete");
	
	private String text;
	
	REQUESTACTIONENUM(String text){
		this.text = text;
	}
	
	public String getText(){
		return this.text;
	}
	
	public static REQUESTACTIONENUM fromString(String text){
		if(text != null){
			for(REQUESTACTIONENUM ra: REQUESTACTIONENUM.values()){
				if(text.equalsIgnoreCase(ra.text)) return ra;
			}
		}
		return null;
	}
}
