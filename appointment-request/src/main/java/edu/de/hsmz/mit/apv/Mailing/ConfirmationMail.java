package edu.de.hsmz.mit.apv.Mailing;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ConfirmationMail implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		DateFormat dateFormatDE = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.GERMAN);
		Calendar webExStartDate = Calendar.getInstance(Locale.GERMAN);
		webExStartDate.add(Calendar.MINUTE, 1);
		
		LOGGER.info("Sending Confirmation-Mail ... '\n");
		LOGGER.info(">>> Mail-Adresse: " + execution.getVariable("email") + " '\n");
		LOGGER.info(">>> Employee responsible: " + execution.getVariable("Assignee_Calc") + " '\n");
		LOGGER.info(">>> Date: " + webExStartDate.getTime().toString() + " '\n");
		
		execution.setVariable("webExStartDateString", dateFormatDE.format(webExStartDate.getTime()));
		execution.setVariable("webExStartDate", "PT1M");
	}

}
