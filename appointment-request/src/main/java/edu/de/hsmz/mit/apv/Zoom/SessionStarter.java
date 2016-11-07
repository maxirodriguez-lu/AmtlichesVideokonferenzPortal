package edu.de.hsmz.mit.apv.Zoom;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import connectjar.org.apache.http.util.ExceptionUtils;

public class SessionStarter implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Starting Zoom-Session ...");
		startSession(execution);
	}

	public void startSession(DelegateExecution execution) {

		try {
			ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
			IdentityService identityService = processEngine.getIdentityService();
			
			String target = "https://api.zoom.us/v1/meeting/create";
			Properties keys = new Properties();
			keys.load(new InputStreamReader(new FileInputStream("zoom-keys.properties")));

			JSONObject json = new JSONObject();
			json.put("api_key", keys.getProperty("Key"));
			json.put("api_secret", keys.getProperty("Secret"));
			json.put("data_type", "JSON");
			json.put("host_id", identityService.getUserInfo((String)execution.getVariable("Assignee_Calc"), "Email"));
			json.put("topic", "Ihr individuelles Beratungsgespräch - " + (String)execution.getVariable("email"));
			
			LOGGER.info(">>> Anfrage an: " + target);
			LOGGER.info(">>> Parameter: " + json.toString(2));
			
			Client client = Client.create();
			WebResource webResource = client.resource(target);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, json.toString());

			if (response.getStatus() != 200) {
				LOGGER.severe(">>> FEHLER BEIM AUFRUF: " + response);
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			LOGGER.info("Antwort von Zoom:");
			LOGGER.info(response.getEntity(String.class));
		} catch (Exception e) {
			LOGGER.severe(Arrays.toString(e.getStackTrace()));
		}
	}

}
