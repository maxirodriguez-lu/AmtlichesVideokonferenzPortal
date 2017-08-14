package edu.de.hsmz.mit.avp.requestHandler;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.de.hsmz.mit.avp.dataHandler.model.LoggedResponse;
import edu.de.hsmz.mit.avp.dataHandler.model.REQUESTACTIONENUM;
import edu.de.hsmz.mit.avp.dataHandler.model.REQUESTTYPEENUM;
import edu.de.hsmz.mit.avp.dataHandler.model.Request;
import edu.de.hsmz.mit.avp.dataHandler.model.STATUSENUM;
import edu.de.hsmz.mit.avp.databaseHandler.Facade;
import edu.de.hsmz.mit.avp.requestHandler.Errors.MissingParameterException;

public class AppointmentHandler {

	
	public static Response handleDataReadRequest(String objektTyp, UriInfo info, String payLoad){
		return handleAppointmentRequest(objektTyp, info, payLoad);
	}
	
	
	public static Response handleAppointmentRequest(String objektTyp, UriInfo info, String payLoad){
		Response resp = null;
		
		try{
			String requestingUser = checkAndGetInputParameter(info, "user");
			
			//Eintrag in fach. Datenbank anlegen
			Request req = new Request(UUID.randomUUID(), 
									  requestingUser, 
									  new Timestamp(System.currentTimeMillis()), 
									  REQUESTACTIONENUM.fromString("Read"), 
									  REQUESTTYPEENUM.fromString(objektTyp), 
									  payLoad != null && payLoad.length() > 0 ? (JSONObject) new JSONParser().parse(payLoad) : null, 
									  info.getRequestUri().toString());

			//Eingang loggen
			long id = Facade.logRequest(req);
			
			HashMap<String, Object> processParams = new HashMap<String, Object>();
			processParams.put("FachlicheID", id);
			
			ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
			ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("appointment-request");
			
			//Warten bis Prozess beendet ist
			while(!instance.isEnded()){
				Thread.sleep(100);
			}
			LoggedResponse loggedResponse = Facade.getReponse(id);
			
			//Ergebnisse aus der fachlichen Datenbank auslesen
			if (loggedResponse != null && loggedResponse.getStatus() != null && loggedResponse.getStatus().equals(STATUSENUM.OKAY)){
				resp = Response.status(200).entity(loggedResponse.getPayLoad().toJSONString()).build();
			}
			else{
				resp = Response.status(404).entity("Fehler beim Aufruf des Services 'appointment-request': \n" + loggedResponse.getPayLoad().toJSONString()).build();
			}
			
		}catch(Exception e){
			resp = Response.status(404).entity("Fehler beim Aufruf des Services 'appointment-request'!").build();
		}
		
		return resp;
	}

	
	private static String checkAndGetInputParameter(UriInfo info, String paramName) throws MissingParameterException{
		String paramValue = info.getQueryParameters(true).getFirst(paramName);
		
		if(paramValue == null || paramValue.length() <= 0){
			throw new MissingParameterException("Fehlender Parameter '" + paramName + "' für Aufruf des Services 'data-request'!");
		}
		
		return paramValue;
	}
	
}
