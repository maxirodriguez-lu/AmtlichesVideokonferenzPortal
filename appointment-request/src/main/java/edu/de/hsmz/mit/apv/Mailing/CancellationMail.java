package edu.de.hsmz.mit.apv.Mailing;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class CancellationMail implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Sending Cancellation-Mail ... '\n");
		LOGGER.info(">>> Mail-Adress: " + execution.getVariable("email") + " '\n");
		LOGGER.info(">>> Employee responsible: " + execution.getVariable("Assignee_Calc") + " '\n");
	}

}
