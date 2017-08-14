package edu.de.hsmz.mit.apv.Data;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.de.hsmz.mit.avp.dataHandler.model.RequestData;
import edu.de.hsmz.mit.avp.dataHandler.model.STATUSENUM;
import edu.de.hsmz.mit.avp.databaseHandler.Facade;

public class DataHandler implements JavaDelegate{

	private final static Logger LOGGER = Logger.getLogger("DATA-REQUESTS");
	
	@SuppressWarnings("unchecked")
	public void execute(DelegateExecution execution) throws Exception {
		
		//Aufruf einer fachlichen Klasse --> Ergebnis in fachliche Welt speichern
		JSONArray result = null;
		STATUSENUM status = STATUSENUM.NA;
		
		Facade databaseHandler = null;
		LOGGER.info(">>> Datarequest received!");
		
		//ID ermitteln
		long id = (long) execution.getVariable("FachlicheID");
		LOGGER.info(">>> ID found: " + id);
		
		try{
			//Zugehörigen Eintrag aus der fachlichen Datenbank auslesen
			databaseHandler = new Facade();
			RequestData req = databaseHandler.getRequestData(id);
			
			//Anhand der Daten prüfen, welche Funktion ausgeführt werden soll
			
			switch (req.getAction()) {
				case READ:
					switch(req.getTyp()){
						case BERATER:
							result = databaseHandler.getSachbearbeiter();
							status = STATUSENUM.OKAY;
							break;
						case SERVICEKATEGORIEN:
							result = databaseHandler.getServicekategorien();
							status = STATUSENUM.OKAY;
							break;
						case SERVICEKATEGORIEEINZELDATEN:
							result = databaseHandler.getServicekategorieEinzeldaten((long) databaseHandler.extractFieldFromPayload(id, "BERATUNGSGEBIET_ID"), 
																					true);
							status = STATUSENUM.OKAY;
							break;
						case SERVICEEINZELDATEN:
							result = databaseHandler.getServiceEinzeldaten((long) databaseHandler.extractFieldFromPayload(id, "BERATUNGSGEBIET_ID"),
																		   (long) databaseHandler.extractFieldFromPayload(id, "SERVICE_ID"),
																		   true,
																		   true);
							status = STATUSENUM.OKAY;
							break;
					case INFORMATIONEINZELDATEN:
						result = databaseHandler.getInformationEinzeldaten((long) databaseHandler.extractFieldFromPayload(id, "ID"),
																		   true);
						status = STATUSENUM.OKAY;
						break;
					case MOEGLICHETERMINE:
						result = databaseHandler.getMoeglicheTermine((long) databaseHandler.extractFieldFromPayload(id, "BERATUNGSGEBIET_ID"),
																	 (long) databaseHandler.extractFieldFromPayload(id, "SERVICE_ID"),
																	 (long) databaseHandler.extractFieldFromPayload(id, "AMTS_ID"),
																	 true);
						status = STATUSENUM.OKAY;
						break;
					case TERMINZUSAMMENFASSUNG:
						result = databaseHandler.getTerminZusammenfassung((long) databaseHandler.extractFieldFromPayload(id, "AMTS_ID"),
								 										  (String) databaseHandler.extractFieldFromPayload(id, "TERMIN_1_DATUM"),
								 										  (String) databaseHandler.extractFieldFromPayload(id, "TERMIN_1_SLOT"),
								 										  (long) databaseHandler.extractFieldFromPayload(id, "TERMIN_1_MA_ID"),
								 										  (String) databaseHandler.extractFieldFromPayload(id, "TERMIN_2_DATUM"),
								 										  (String) databaseHandler.extractFieldFromPayload(id, "TERMIN_2_SLOT"),
								 										  (long) databaseHandler.extractFieldFromPayload(id, "TERMIN_2_MA_ID"),
								 										  (String) databaseHandler.extractFieldFromPayload(id, "TERMIN_3_DATUM"),
								 										  (String) databaseHandler.extractFieldFromPayload(id, "TERMIN_3_SLOT"),
								 										  (long) databaseHandler.extractFieldFromPayload(id, "TERMIN_3_MA_ID"));
						status = STATUSENUM.OKAY;
						break;
					default:
						throw new RuntimeException(String.format("Es existiert keine Operation des Typs '%s' für die Aktion '%s'.'" + 
					                                             "\nDer Aufruf ist in der übergebenen Form ungültig und wird nicht bearbeitet!",
					                                             req.getAction().getText(),
					                                             req.getTyp().getText())
												  );
					}
					
					break;
			case ADD:
				switch(req.getTyp()){
					case TERMINANLEGEN:
						result = databaseHandler.addAppointment((long) databaseHandler.extractFieldFromPayload(id, "BERATUNGSGEBIET_ID"),
																(long) databaseHandler.extractFieldFromPayload(id, "SERVICE_ID"),
																(long) databaseHandler.extractFieldFromPayload(id, "AMTSART_ID"),
																(long) databaseHandler.extractFieldFromPayload(id, "AMT_ID"),
																(long) databaseHandler.extractFieldFromPayload(id, "MITARBEITER_ID"),
																(String) databaseHandler.extractFieldFromPayload(id, "TERMIN_DATUMUNDUHRZEIT"),
																(long) databaseHandler.extractFieldFromPayload(id, "TERMIN_GROUP_ID")
								);
						status = STATUSENUM.OKAY;
						break;
					default:
						throw new RuntimeException(String.format("Es existiert keine Operation des Typs '%s' für die Aktion '%s'.'" + 
                                "\nDer Aufruf ist in der übergebenen Form ungültig und wird nicht bearbeitet!",
                                req.getAction().getText(),
                                req.getTyp().getText())
				  );
				}
				break;
			case DELETE:
				break;
			case UPDATE:
				break;
			default:
				break;
			}
		}catch(Exception e){
			status = STATUSENUM.ERROR;
			
			result = new JSONArray();
			JSONObject error = new JSONObject();
			error.put("ERRORCODE", e.getMessage());
			result.add(error);
		}finally{
			//Ergebnis in die fachl. Datenbank loggen
			if(databaseHandler == null) databaseHandler = new Facade();
			databaseHandler.logResponse(status, result, id);
		}
		
	}

}
