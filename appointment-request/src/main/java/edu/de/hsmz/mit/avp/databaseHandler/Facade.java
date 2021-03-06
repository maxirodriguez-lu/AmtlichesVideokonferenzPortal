package edu.de.hsmz.mit.avp.databaseHandler;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.de.hsmz.mit.avp.dataHandler.model.LoggedResponse;
import edu.de.hsmz.mit.avp.dataHandler.model.REQUESTACTIONENUM;
import edu.de.hsmz.mit.avp.dataHandler.model.REQUESTTYPEENUM;
import edu.de.hsmz.mit.avp.dataHandler.model.Request;
import edu.de.hsmz.mit.avp.dataHandler.model.RequestData;
import edu.de.hsmz.mit.avp.dataHandler.model.STATUSENUM;

public class Facade {
	SimpleDateFormat dateFormat_dateOnly = new SimpleDateFormat("dd.MM.yyyy");
	SimpleDateFormat dateFormat_dateOnly_sql = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat dateFormat_timeOnly = new SimpleDateFormat("HH:mm:ss");
	SimpleDateFormat dateFormat_dateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	int slotDauer = 30;
	
	public static long logRequest(Request req){
		String insertRequestSQL = "INSERT INTO PUBLIC.LOGGING (REQUEST_UUID, REQUEST_CALLERNAME, REQUEST_TIMESTAMP, REQUEST_TYPE, REQUEST_DATAOBJEKT, REQUEST_FULLURL, REQUEST_PAYLOAD, RESPONSE_STATUS) " +
		        					      "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
		
		try(
				Connection conn = getFachlicheDatenbank();	
				PreparedStatement insertPreparedStatement = conn.prepareStatement(insertRequestSQL, Statement.RETURN_GENERATED_KEYS);
			){

			insertPreparedStatement.setString(1, req.getId().toString());
			insertPreparedStatement.setString(2, req.getCaller());
			insertPreparedStatement.setObject(3, req.getRequestTime());
			insertPreparedStatement.setString(4, req.getRequestType().getText());
			insertPreparedStatement.setString(5, req.getRequestAction().getText());
			insertPreparedStatement.setString(6, req.getRequestUrl());
			insertPreparedStatement.setCharacterStream(7, req.getPayLoad() != null ? new StringReader(req.getPayLoad().toJSONString()) : new StringReader(""));
			insertPreparedStatement.setString(8, "NA");
			
			int affectedRows = insertPreparedStatement.executeUpdate();
	        if (affectedRows == 0) {
	            throw new SQLException("Datenanlage fehlgeschlage, Daten fehlerhaft!");
	        }
	        
	        ResultSet generatedKeys = insertPreparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }else {
	            throw new SQLException("Datenanlage fehlgeschlage, Daten fehlerhaft!");
            }
		} catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
		}
	}
	
	private static Connection getFachlicheDatenbank() {

		Connection conn = null;
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/FachlicheDb");

			conn = ds.getConnection();
			return conn;
		} catch (Exception e) {
			return null;
		}
	}

	public static LoggedResponse getReponse(long id) {
		String selectSQL = "SELECT RESPONSE_STATUS, RESPONSE_PAYLOAD, REPONSE_TIMESTAMP FROM PUBLIC.LOGGING " +
			      "WHERE REQUEST_ID = " + id + ";";

		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL);
		){
			ResultSet results = selectPreparedStatement.executeQuery();
			if (results.next()) {
				LoggedResponse loggedResult = new LoggedResponse(STATUSENUM.fromString(results.getString(1)), 
																 results.getString(2) != null ? (JSONObject) new JSONParser().parse(results.getString(2)) : null, 
																 results.getTimestamp(3));
				return loggedResult;
			}else {
	            throw new SQLException("Datenauslesen fehlgeschlage, Daten fehlerhaft!");
            }

		} catch (SQLException e) {
	        throw new RuntimeException(e.getMessage());
		} catch (ParseException e) {
	        throw new RuntimeException(e.getMessage());
		}
	}

	public RequestData getRequestData(long id) {
		String selectSQL = "SELECT REQUEST_TYPE, REQUEST_DATAOBJEKT FROM PUBLIC.LOGGING " +
			      "WHERE REQUEST_ID = " + id + ";";

		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL);
		){
			ResultSet results = selectPreparedStatement.executeQuery();
			if (results.next()) {
				RequestData loggedRequest = new RequestData(results.getString(2) != null ? REQUESTACTIONENUM.fromString(results.getString(2)) : null,
															results.getString(1) != null ? REQUESTTYPEENUM.fromString(results.getString(1)) : null);
				return loggedRequest;
			}else {
	            throw new SQLException("Datenauslesen fehlgeschlage, Daten fehlerhaft!");
          }

		} catch (SQLException e) {
	        throw new RuntimeException(e.getMessage());
		} 
	}

	@SuppressWarnings("unchecked")
	public void logResponse(STATUSENUM status, JSONArray result, long id) {
		String updateRequestSQL = "UPDATE PUBLIC.LOGGING SET RESPONSE_STATUS = ?, RESPONSE_PAYLOAD = ?, REPONSE_TIMESTAMP = ? "
				+ "WHERE REQUEST_ID = " + id + ";";

		try (Connection conn = getFachlicheDatenbank();
			PreparedStatement updatePreparedStatement = conn.prepareStatement(updateRequestSQL)){

			JSONObject resultWrapper = new JSONObject();
			resultWrapper.put("RESULT", result);
			
			updatePreparedStatement.setString(1, status.getText());
			updatePreparedStatement.setString(2, resultWrapper.toJSONString());
			updatePreparedStatement.setObject(3, new Timestamp(System.currentTimeMillis()));

			int affectedRows = updatePreparedStatement.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Datenupdate fehlgeschlage, Daten fehlerhaft!");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	
	public void getTermineAndAddRetrievedDataToExecution(String group_id, DelegateExecution execution) {
		
		String selectSQL = "SELECT KUNDE_EMAIL, KUNDE_ANREDE, KUNDE_VORNAME, KUNDE_NAME, BERATER_ID, DATUM, UHRZEIT, ID, ANFRAGE, GROUP_ID FROM PUBLIC.TERMINE WHERE GROUP_ID = '" + group_id + "';";
		
		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL);
		){
			ResultSet dbResults = selectPreparedStatement.executeQuery();
			for(int i = 1; dbResults.next(); i++){
				
				execution.setVariable("Kunde_Email", dbResults.getString(1));
				execution.setVariable("Kunde_Anrede", dbResults.getString(2));
				execution.setVariable("Kunde_Vorname", dbResults.getString(3));
				execution.setVariable("Kunde_Nachname", dbResults.getString(4));
				
				execution.setVariable("Termin" + i + "_ID", dbResults.getString(8));
				execution.setVariable("Termin" + i + "_Datum", dbResults.getString(6));
				execution.setVariable("Termin" + i + "_Uhrzeit", dbResults.getString(7));
				execution.setVariable("Termin" + i + "Sachbearbeiter", dbResults.getString(5));

				execution.setVariable("Anfrage_Text", dbResults.getString(9));
				execution.setVariable("Anfrage_ID", dbResults.getString(10));
			}
		} catch (Exception e) {
	        throw new RuntimeException(e.getMessage());
		} 
	}

	public Object extractFieldFromPayload(long id, String fieldName) {
		String selectSQL = "SELECT REQUEST_ID, REQUEST_PAYLOAD FROM PUBLIC.LOGGING " +
			      		   "WHERE REQUEST_ID = " + id + ";";
	
		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL);
		){
			ResultSet results = selectPreparedStatement.executeQuery();
			if (results.next()) {
				
				String payloadTxt = IOUtils.toString(results.getCharacterStream(2));
				if(payloadTxt != null && payloadTxt.length() > 0){
					JSONObject payload = (JSONObject) new JSONParser().parse(payloadTxt);
					
					if(payload.containsKey("variables")){
						if(payload.get("variables") instanceof JSONObject){
							JSONObject variables = (JSONObject) payload.get("variables");
							if(variables.containsKey(fieldName)){
								return variables.get(fieldName);
							}else{
					            throw new SQLException(String.format("Im Request-Payload des Eintrags mit ID '%s' konnte kein Eintrag '%s' gefunden werden!", id, fieldName));
							}
						}else{
				            throw new SQLException(String.format("Illegaler Eintragstyp in den Variablen des Request-Payload des Eintrags mit ID '%s'!", id));
						}
					}else{
			            throw new SQLException(String.format("Im Request-Payload des Eintrags mit ID '%s' konnte kein Eintrag 'variables' gefunden werden!", id));
					}
				}else{
		            throw new SQLException(String.format("Request-Payload des Eintrags mit ID '%s' konnte nicht gelesen werden!", id));
				}
			}else {
	            throw new SQLException("Datenauslesen fehlgeschlage, Daten fehlerhaft!");
	    }
	
		} catch (SQLException e) {
	        throw new RuntimeException(e.getMessage());
		} catch (ParseException e) {
	        throw new RuntimeException(String.format("Request-Payload des Eintrags mit ID '%s' ist korrupt!", id));
		} catch (IOException e) {
	        throw new RuntimeException(e.getMessage());
		} 
	}

	public String getTypZuBeraterID(String requestedUserId) {
		
		String selectSQL = "SELECT ID, TYP FROM PUBLIC.BERATER WHERE ID = " + requestedUserId + ";";
		
		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL);
		){
			ResultSet dbResults = selectPreparedStatement.executeQuery();
			if(dbResults.first()){
				return dbResults.getString(2);
			}else{
				return "";
			}
		} catch (Exception e) {
	        throw new RuntimeException(e.getMessage());
		}
	}

	public String getCamundaUserZuBeraterID(String requestedUserId) {
		
		String selectSQL = "SELECT ID, CAMUNDA_USERNAME FROM PUBLIC.BERATER WHERE ID = " + requestedUserId + ";";
		
		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL);
		){
			ResultSet dbResults = selectPreparedStatement.executeQuery();
			if(dbResults.first()){
				return dbResults.getString(2);
			}else{
				return "";
			}
		} catch (Exception e) {
	        throw new RuntimeException(e.getMessage());
		}
	}
}
