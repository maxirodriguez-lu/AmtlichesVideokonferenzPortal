package edu.de.hsmz.mit.avp.dataHandler.model;

import java.sql.Timestamp;
import java.util.UUID;

import org.json.simple.JSONObject;

public class Request {
	private UUID id;
	private String caller;
	private Timestamp requestTime;
	private REQUESTACTIONENUM requestAction;
	private REQUESTTYPEENUM requestType;
	private JSONObject payLoad; 
	private String requestUrl;
	
	public Request(UUID id, 
				   String caller, 
				   Timestamp requestTime, 
				   REQUESTACTIONENUM requestAction,
				   REQUESTTYPEENUM requestType, 
				   JSONObject payLoad,
				   String requestUrl) {
		super();
		
		this.id = id;
		this.caller = caller;
		this.requestTime = requestTime;
		this.requestAction = requestAction;
		this.requestType = requestType;
		this.payLoad = payLoad;
		this.requestUrl = requestUrl;
	}

	public REQUESTACTIONENUM getRequestAction() {
		return requestAction;
	}

	public void setRequestAction(REQUESTACTIONENUM requestAction) {
		this.requestAction = requestAction;
	}

	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id) {
		this.id = id;
	}
	
	public String getCaller() {
		return caller;
	}
	
	public void setCaller(String caller) {
		this.caller = caller;
	}
	
	public Timestamp getRequestTime() {
		return requestTime;
	}
	
	public void setRequestTime(Timestamp requestTime) {
		this.requestTime = requestTime;
	}
	
	public REQUESTTYPEENUM getRequestType() {
		return requestType;
	}
	
	public void setRequestType(REQUESTTYPEENUM requestType) {
		this.requestType = requestType;
	}
	
	public JSONObject getPayLoad() {
		return payLoad;
	}
	
	public void setPayLoad(JSONObject payLoad) {
		this.payLoad = payLoad;
	}
	
	public String getRequestUrl() {
		return requestUrl;
	}
	
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
}
