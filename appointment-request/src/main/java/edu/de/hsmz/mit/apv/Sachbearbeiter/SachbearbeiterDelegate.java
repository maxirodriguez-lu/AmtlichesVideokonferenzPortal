package edu.de.hsmz.mit.apv.Sachbearbeiter;

import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;

public class SachbearbeiterDelegate implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Processing request by '" + execution.getVariable("customerId") + "");

		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = processEngine.getIdentityService();
		
		String requestedUserId = (String) execution.getVariable( "Assignee_Req" );
		LOGGER.info(">>> Requested Sachbearbeiter: " + requestedUserId);
		LOGGER.info(">>> Checking Sachbearbeiter ...");
		
		List<User> userList = identityService.createUserQuery().memberOfGroup("Sachbearbeiter").list();
		User selectedUser = null;
		for(User user: userList){
			if(user.getId().equalsIgnoreCase(requestedUserId)){
				selectedUser = user;
			}
		}
		if(selectedUser != null){
			LOGGER.info("Selecting User: " + selectedUser.getFirstName() + " " + selectedUser.getLastName());
		}else{
			LOGGER.warning("Requested User not found - no User will be set. Task needs to be claimed!");
		}
		
		execution.setVariable("Assignee_Calc", selectedUser.getId());
	}

}
