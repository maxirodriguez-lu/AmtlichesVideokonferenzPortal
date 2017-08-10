package edu.de.hsmz.mit.avp.dataHandler.model;

import java.sql.Timestamp;

import org.json.simple.JSONObject;

public class LoggedResponse {
	
	private STATUSENUM status;
	private JSONObject payLoad;
	private Timestamp timestamp;
	
	public LoggedResponse(STATUSENUM status, JSONObject payLoad, Timestamp timestamp) {
		super();
		this.status = status;
		this.payLoad = payLoad;
		this.timestamp = timestamp;
	}
	public STATUSENUM getStatus() {
		return status;
	}
	public JSONObject getPayLoad() {
		return payLoad;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
}
