package edu.de.hsmz.mit.avp.databaseHandler;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
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

	@SuppressWarnings("unchecked")
	public JSONArray getSachbearbeiter() {
		
		String selectSQL = "SELECT ID, NAME, VORNAME, EMAIL, TELEFON FROM PUBLIC.BERATER WHERE TYP = 'SACHBEARBEITER';";
		JSONArray results = new JSONArray();
		
		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL);
		){
			ResultSet dbResults = selectPreparedStatement.executeQuery();
			while (dbResults.next()) {
				JSONObject result = new JSONObject();
				
				result.put("ID", dbResults.getLong(1));
				result.put("NACHNAME", dbResults.getString(2));
				result.put("VORNAME", dbResults.getString(3));
				result.put("NAME", dbResults.getString(3).substring(0, 1) + "." + dbResults.getString(2));
				result.put("EMAIL", dbResults.getString(4));
				result.put("TELEFON", dbResults.getString(5));
				
				results.add(result);
			}
			
			return results;
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("ERRORCODE", e.getMessage());
			results.add(result);
			
	        throw new RuntimeException(e.getMessage());
		} 
	}

	@SuppressWarnings("unchecked")
	public JSONArray getServicekategorien() {
		
		String selectSQL = "SELECT ID, NAME, BESCHREIBUNG, IMAGE_PROFILE FROM PUBLIC.BERATUNGSTHEMEN_GEBIETE;";
		JSONArray results = new JSONArray();
		
		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL);
		){
			ResultSet dbResults = selectPreparedStatement.executeQuery();
			while (dbResults.next()) {
				JSONObject result = new JSONObject();
				result.put("ID", dbResults.getLong(1));
				result.put("NAME", dbResults.getString(2));
				result.put("BESCHREIBUNG", dbResults.getString(3));
				result.put("IMAGE", dbResults.getString(4));
				results.add(result);
			}
			
			return results;
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("ERRORCODE", e.getMessage());
			results.add(result);
			
	        throw new RuntimeException(e.getMessage());
		} 
	}

	

	@SuppressWarnings("unchecked")
	public JSONArray getServiceEinzeldaten(long id) {
		String selectSQL_Themengebiet = "SELECT ID, NAME, BESCHREIBUNG, IMAGE_HEADER FROM PUBLIC.BERATUNGSTHEMEN_GEBIETE WHERE ID = " + id + ";";
		String selectSQL_Services = "SELECT ID, NAME, TAGS, AMTSART_ID, BERATUNGSTHEMA_GEBIET_ID FROM PUBLIC.BERATUNGSTHEMEN_SERVICES WHERE BERATUNGSTHEMA_GEBIET_ID = " + id + ";";
		
		JSONObject result = new JSONObject();
		
		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL_Themengebiet);
		){
			ResultSet dbResults = selectPreparedStatement.executeQuery();
			if (dbResults.first()) {
				result.put("ID", dbResults.getLong(1));
				result.put("NAME", dbResults.getString(2));
				result.put("BESCHREIBUNG", dbResults.getString(3));
				result.put("IMAGE", dbResults.getString(4));
			}
			
		} catch (Exception e) {
	        throw new RuntimeException(e.getMessage());
		} 
		

		try(
			Connection conn = getFachlicheDatenbank();	
			PreparedStatement selectPreparedStatement = conn.prepareStatement(selectSQL_Services);
		){
			ResultSet dbResults = selectPreparedStatement.executeQuery();
			JSONArray serviceList = new JSONArray();
			while (dbResults.next()) {
				JSONObject service = new JSONObject();
				
				service.put("ID", dbResults.getLong(1));
				service.put("NAME", dbResults.getString(2));
				service.put("TAGS", dbResults.getString(3));
				service.put("AMTSART_ID", dbResults.getLong(4));
				
				serviceList.add(service);
			}
			result.put("SERVICES", serviceList);
		} catch (Exception e) {
	        throw new RuntimeException(e.getMessage());
		} 
		
		JSONArray res = new JSONArray();
		res.add(result);
		
		return res;
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
					            throw new SQLException(String.format("Im Request-Payload des Eintrags mit ID '%n' konnte kein Eintrag '%s' gefunden werden!", id, fieldName));
							}
						}else{
				            throw new SQLException(String.format("Illegaler Eintragstyp in den Variablen des Request-Payload des Eintrags mit ID '%n'!", id, fieldName));
						}
					}else{
			            throw new SQLException(String.format("Im Request-Payload des Eintrags mit ID '%n' konnte kein Eintrag 'variables' gefunden werden!", id, fieldName));
					}
				}else{
		            throw new SQLException(String.format("Request-Payload des Eintrags mit ID '%n' konnte nicht gelesen werden!", id));
				}
			}else {
	            throw new SQLException("Datenauslesen fehlgeschlage, Daten fehlerhaft!");
        }

		} catch (SQLException e) {
	        throw new RuntimeException(e.getMessage());
		} catch (ParseException e) {
            throw new RuntimeException(String.format("Request-Payload des Eintrags mit ID '%n' ist korrupt!", id));
		} catch (IOException e) {
	        throw new RuntimeException(e.getMessage());
		} 
	}
}
