package edu.de.hsmz.mit.apv.Mailing;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ConfirmationMail implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Sending Confirmation-Mail ... '\n");

		SimpleDateFormat dateFormat_dateTimeISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		SimpleDateFormat dateFormat_dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Calendar webExStartDate = Calendar.getInstance(Locale.GERMAN);
		Calendar webExReminderDate = Calendar.getInstance(Locale.GERMAN);
		
		//Ausgewählten Termin auslesen
		int terminNr = (int) execution.getVariable("bestaetigung_annahme");
		
		String webExStartDateString = execution.getVariable("Termin" + terminNr + "_Datum") + " " + execution.getVariable("Termin" + terminNr + "_Uhrzeit");
		
		webExStartDate.setTime(dateFormat_dateTime.parse(webExStartDateString));
		
		webExReminderDate.setTime(dateFormat_dateTime.parse(webExStartDateString));
		webExReminderDate.add(Calendar.MINUTE, -15);
		
		execution.setVariable("Konferenz_Zeitpunkt_Erinnerung", dateFormat_dateTimeISO8601.format(webExReminderDate.getTime()));
		execution.setVariable("Konferenz_Zeitpunkt_Start", dateFormat_dateTimeISO8601.format(webExStartDate.getTime()));
	}

}
