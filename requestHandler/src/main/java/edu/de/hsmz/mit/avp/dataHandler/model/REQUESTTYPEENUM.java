package edu.de.hsmz.mit.avp.dataHandler.model;

public enum REQUESTTYPEENUM {
	//DATA
	SERVICEKATEGORIEN ("Servicekategorien"),
	SERVICEKATEGORIEEINZELDATEN ("ServicekategorieEinzeldaten"),
	SERVICEEINZELDATEN("ServiceEinzeldaten"),
	INFORMATIONEINZELDATEN("InformationEinzeldaten"),
	MOEGLICHETERMINE("MoeglicheTermine"),
	TERMINANLEGEN("TerminAnlegen"),
	BERATER ("Berater"),
	TERMINZUSAMMENFASSUNG("TerminZusammenfassung"),
	//APPOINTMENT
	APOOINTMENT_START("start");
	
	private String text;
	
	REQUESTTYPEENUM(String text){
		this.text = text;
	}
	
	public String getText(){
		return this.text;
	}
	
	public static REQUESTTYPEENUM fromString(String text){
		if(text != null){
			for(REQUESTTYPEENUM rt: REQUESTTYPEENUM.values()){
				if(text.equalsIgnoreCase(rt.text)) return rt;
			}
		}
		return null;
	}
}
