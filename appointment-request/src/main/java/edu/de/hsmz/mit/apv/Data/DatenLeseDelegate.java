package edu.de.hsmz.mit.apv.Data;

import java.sql.ResultSet;
import java.util.logging.Logger;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import edu.de.hsmz.mit.avp.databaseHandler.Facade;

public class DatenLeseDelegate implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Processing request by '" + execution.getVariable("customerId") + "");

		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = processEngine.getIdentityService();
		
		//Über fachliche Db die ID holen und aus der Datenbank den Ansprechpartner auslesen
		long fachlicheId = (long) execution.getVariable( "FachlicheID" );
		
		Facade databaseHandler = new Facade();
		String appointment_ID = (String) databaseHandler.extractFieldFromPayload(fachlicheId, "APPOINTMENT_ID");
		
		
		ResultSet daten = databaseHandler.getTermine(appointment_ID);
		//"SELECT KUNDE_EMAIL, KUNDE_ANREDE, KUNDE_VORNAME, KUNDE_NAME, BERATER_ID, DATUM, UHRZEIT FROM PUBLIC.TERMINE WHERE GROUP_ID = '" + group_id + "';"

		execution.setVariable("Kunde_Email", daten.getString(1));
		execution.setVariable("Kunde_Anrede", daten.getString(1));
		execution.setVariable("Kunde_Vorname", daten.getString(1));
		execution.setVariable("Kunde_Nachname", daten.getString(1));

		execution.setVariable("Anfrage_Text", daten.getString(1));
		execution.setVariable("Anfrage_ID", daten.getString(1));
		
		execution.setVariable("Termin1_ID", daten.getString(1));
		execution.setVariable("Termin1_Datum", daten.getString(1));
		execution.setVariable("Termin1_Uhrzeit", daten.getString(1));
		execution.setVariable("Termin1_Sachbearbeiter", daten.getString(1));
		execution.setVariable("Termin2_ID", daten.getString(1));
		execution.setVariable("Termin2_Datum", daten.getString(1));
		execution.setVariable("Termin2_Uhrzeit", daten.getString(1));
		execution.setVariable("Termin2_Sachbearbeiter", daten.getString(1));
		execution.setVariable("Termin3_ID", daten.getString(1));
		execution.setVariable("Termin3_Datum", daten.getString(1));
		execution.setVariable("Termin3_Uhrzeit", daten.getString(1));
		execution.setVariable("Termin3_Sachbearbeiter", daten.getString(1));
	}

}
