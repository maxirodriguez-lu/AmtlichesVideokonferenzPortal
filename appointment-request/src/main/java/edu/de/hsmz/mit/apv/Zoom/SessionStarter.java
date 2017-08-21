package edu.de.hsmz.mit.apv.Zoom;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;


public class SessionStarter implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Starting Zoom-Session ...");
		
		try{
			LOGGER.info("> Ermittlung User ...");
			String host_id = getUserIdForMailAdress((String) execution.getVariable("Sachbearbeiter_Email"));
			
			LOGGER.info("> Starte Session ...");
			HashMap<String, String> meetingAccessInformation = startMeeting(host_id);
			
			LOGGER.info("> Schreibe Ergebnisse in Prozess-Instanz ...");
			for (Entry<String, String> entry : meetingAccessInformation.entrySet()) {
				execution.setVariable("Meeting_" + entry.getKey(), entry.getValue());
			}
		}catch(Exception e){
			execution.setVariable("Meeting_ErrorOnCreation", e);
			throw e;
		}
	}


	private String getUserIdForMailAdress(String email) {
		String id ="";
	
		try {
			String target = "https://api.zoom.us/v1/user/list";
			Properties keys = new Properties();
			try (final InputStream stream = this.getClass().getResourceAsStream("/zoom-keys.properties")) {
				keys.load(stream);
			}

			MultivaluedMap<String, String> params = new MultivaluedMapImpl();
			params.add("api_key", keys.getProperty("Key"));
			params.add("api_secret", keys.getProperty("Secret"));

			LOGGER.info(">>> Anfrage an: " + target);
			LOGGER.info(">>> Parameter: " + params.toString());

			Client client = Client.create();
			WebResource webResource = client.resource(target);

			ClientResponse response = webResource
										.queryParams(params)
										.type("application/json")
										.post(ClientResponse.class);

			if (response.getStatus() != 200) {
				LOGGER.severe(">>> FEHLER BEIM AUFRUF: " + response);
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}else{
				String respTxt = response.getEntity(String.class);
				
				LOGGER.info(">>> Antwort von Zoom:");
				LOGGER.info(">>> " + respTxt + "\n\n");
				
				JSONParser parser = new JSONParser();
				JSONObject respPayLoad = (JSONObject) parser.parse(respTxt);
				
				if(respPayLoad.containsKey("users")){
					if(respPayLoad.get("users").getClass() == JSONArray.class){
						JSONArray users = (JSONArray) respPayLoad.get("users");
						
						@SuppressWarnings("unchecked")
						Iterator<JSONObject> userIterator = users.iterator();
						while(userIterator.hasNext()){
							JSONObject user = userIterator.next();
							if(email.equalsIgnoreCase((String) user.get("email"))){
								id = (String) user.get("id");
							}
						}
					}else if(respPayLoad.get("users").getClass() == JSONObject.class){
						JSONObject user = (JSONObject) respPayLoad.get("users");
						if(email.equalsIgnoreCase((String) user.get("email"))){
							id = (String) user.get("id");
						}
					}else{
						throw new RuntimeException("Liste der Zoom-User unlesbar!");
					}

					if(id.length() == 0){
						throw new RuntimeException(String.format("Es ist keine Zoom-User mit Email '%s' angelegt!", email));
					}else{
						LOGGER.info(">>> Aufruf erfolgreich! \n\n");
					}
				}else{
					throw new RuntimeException("Es sind keine Zoom-User angelegt!");
				}
			}

		} catch (Exception e) {
			LOGGER.severe("Fehler beim Auslesen der vorhandenen Zoom-User: \n" + e.getMessage() + "\n\n");
			throw new RuntimeException("Fehler beim Auslesen der vorhandenen Zoom-User!", e);
		}
		
		return id;
	}

	private HashMap<String, String> startMeeting(String id) {
		HashMap<String, String> meetingAccessPoints = new HashMap<String, String>();
		
		try {
			String target = "https://api.zoom.us/v1/meeting/create";
			Properties keys = new Properties();
			try (final InputStream stream = this.getClass().getResourceAsStream("/zoom-keys.properties")) {
				keys.load(stream);
			}

			MultivaluedMap<String, String> params = new MultivaluedMapImpl();
			params.add("api_key", keys.getProperty("Key"));
			params.add("api_secret", keys.getProperty("Secret"));
			params.add("data_type", "JSON");
			params.add("host_id", id);
			params.add("type", "1");
			params.add("topic", "Ihr individuelles Beratungsgespräch - maximilian.troesch@students.hs-mainz.de");

			LOGGER.info(">>> Anfrage an: " + target);
			LOGGER.info(">>> Parameter: " + params.toString() + "\n\n");

			Client client = Client.create();
			WebResource webResource = client.resource(target);

			ClientResponse response = webResource
										.queryParams(params)
										.type("application/json")
										.post(ClientResponse.class);

			if (response.getStatus() != 200) {
				LOGGER.info(">>> FEHLER BEIM AUFRUF: " + response);
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}else{
				String respTxt = response.getEntity(String.class);
				
				LOGGER.info(">>> Antwort von Zoom:");
				LOGGER.info(">>> " + respTxt  + "\n\n");
				
				JSONParser parser = new JSONParser();
				JSONObject respPayLoad = (JSONObject) parser.parse(respTxt);
				
				if(respPayLoad.containsKey("id")){
					meetingAccessPoints.put("id", ((Long) respPayLoad.get("id")).toString());
				}else{
					throw new RuntimeException("Es konnte keine ID für das neu angelegte Meeting ermittelt werden!");
				}

				if(respPayLoad.containsKey("uuid")){
					meetingAccessPoints.put("uuid", (String) respPayLoad.get("uuid"));
				}else{
					throw new RuntimeException("Es konnte keine UUID für das neu angelegte Meeting ermittelt werden!");
				}

				if(respPayLoad.containsKey("start_url")){
					meetingAccessPoints.put("start_url", (String) respPayLoad.get("start_url"));
				}else{
					throw new RuntimeException("Es konnte keine Start-URL für das neu angelegte Meeting ermittelt werden!");
				}

				if(respPayLoad.containsKey("join_url")){
					meetingAccessPoints.put("join_url", (String) respPayLoad.get("join_url"));
				}else{
					throw new RuntimeException("Es konnte keine Join-Url für das neu angelegte Meeting ermittelt werden!");
				}

				if(respPayLoad.containsKey("host_id")){
					meetingAccessPoints.put("host_id", (String) respPayLoad.get("host_id"));
				}else{
					throw new RuntimeException("Es konnte keine Host-ID für das neu angelegte Meeting ermittelt werden!");
				}

				if(respPayLoad.containsKey("password")){
					meetingAccessPoints.put("password", (String) respPayLoad.get("password"));
				}else{
					throw new RuntimeException("Es konnte kein Passwort für das neu angelegte Meeting ermittelt werden!");
				}

				if(meetingAccessPoints.isEmpty()){
					throw new RuntimeException(String.format("Es konnte kein Zoom-Meeting angelegt werden!"));
				}else{
					LOGGER.info(">>> Aufruf erfolgreich! \n\n");
				}
			}
		} catch (Exception e) {
			LOGGER.severe("Fehler beim Starten eines neuen Zoom-Meetings: \n" + e.getMessage() + "\n\n");
			throw new RuntimeException("Fehler beim Starten eines neuen Zoom-Meetings!", e);
		}

		return meetingAccessPoints;
	}

}
