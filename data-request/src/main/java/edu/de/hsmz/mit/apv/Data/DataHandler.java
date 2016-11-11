package edu.de.hsmz.mit.apv.Data;

import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.json.simple.JSONArray;

public class DataHandler implements JavaDelegate{

	private final static Logger LOGGER = Logger.getLogger("DATA-REQUESTS");
	private final static String userGroup = "Sachbearbeiter";
	
	@SuppressWarnings("unchecked")
	public void execute(DelegateExecution execution) throws Exception {
		
		LOGGER.info(">>> Datarequest received!");

		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = processEngine.getIdentityService();

		JSONObject payLoad = new JSONObject();
		
		List<User> userList = identityService.createUserQuery().memberOfGroup(userGroup).list();
		if(userList.size() > 0){
			JSONArray userArray = new JSONArray();
			payLoad.append("Users", userArray);
			
			for(User u: userList){
				JSONObject user = new JSONObject();
				user.append("FirstName", u.getFirstName());
				user.append("LastName", u.getLastName());
				user.append("Email", u.getEmail());
				user.append("Id", u.getId());
				
				userArray.add(user);
			}
		}else{
			throw new RuntimeException(String.format("Es sind keine Anwender in der Gruppe '%s' vorhanden!", userGroup));
		}
		
		execution.setVariable("payload", payLoad.toString());
	}

}
