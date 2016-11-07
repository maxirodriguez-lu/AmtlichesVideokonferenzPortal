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
		
		List<User> userList = identityService.createUserQuery().memberOfGroup("Sachbearbeiter").list();
		long selectUser = Math.round(1 + Math.random() * (userList.size()-2));
		User selectedUser = userList.get((int) selectUser);
		LOGGER.info("Selecting User: " + selectedUser.getFirstName() + " " + selectedUser.getLastName());
		
		execution.setVariable("Assignee_Calc", selectedUser.getId());
	}

}
