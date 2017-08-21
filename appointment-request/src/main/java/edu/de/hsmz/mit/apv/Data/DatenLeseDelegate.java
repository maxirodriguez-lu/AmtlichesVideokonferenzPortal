package edu.de.hsmz.mit.apv.Data;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import edu.de.hsmz.mit.avp.databaseHandler.Facade;

public class DatenLeseDelegate implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("APPOINTMENT-REQUESTS");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Starte Prozessschritt 'Daten auslesen ...'");
		
		//Über fachliche Db die ID holen und aus der Datenbank den Ansprechpartner auslesen
		long fachlicheId = (long) execution.getVariable( "FachlicheID" );
		
		Facade databaseHandler = new Facade();
		String appointment_ID = (String) databaseHandler.extractFieldFromPayload(fachlicheId, "APPOINTMENT_ID");
		
		databaseHandler.getTermineAndAddRetrievedDataToExecution(appointment_ID, execution);
	}

}
