package edu.de.hsmz.mit.avp.requestHandler;

import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class AppointmentHandler {

	public static Response handleRequest(){
		try{
			ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
			@SuppressWarnings("unused")
			ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("appointment-request");

			return Response.status(200).entity("Appointment-Request received").build();
		}catch(Exception e){
			return Response.status(404).entity("Fehler beim Aufruf des Services 'appointment-request'!").build();
		}
	}
	
}
