package edu.de.hsmz.mit.apv.Sachbearbeiter;

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;

import edu.de.hsmz.mit.avp.databaseHandler.Facade;

public class SachbearbeiterDelegate implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Starte Prozessschritt 'Sachbearbeiter ermitteln ...'");

		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = processEngine.getIdentityService();
				
		Facade databaseHandler = new Facade();
		
		String requestedUserId = databaseHandler.getCamundaUserZuBeraterID((String) execution.getVariable("Termin1Sachbearbeiter"));
		String requestedUserTyp = databaseHandler.getTypZuBeraterID((String) execution.getVariable("Termin1Sachbearbeiter"));
		
		List<User> userList = identityService.createUserQuery().memberOfGroup(StringUtils.capitalize(requestedUserTyp.toLowerCase())).list();
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
		
		if(selectedUser != null){
			execution.setVariable("Sachbearbeiter", selectedUser.getId());
			execution.setVariable("Sachbearbeiter_Email", selectedUser.getEmail());
		}
	}

}
