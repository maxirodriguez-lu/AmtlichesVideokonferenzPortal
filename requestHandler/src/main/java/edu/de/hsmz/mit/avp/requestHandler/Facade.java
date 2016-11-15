package edu.de.hsmz.mit.avp.requestHandler;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;

@Path("/request")
public class Facade {

	@GET
	@Path("/appointment")
	public Response handleAppointment() {
		try{
			ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
			ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("appointment-request");

			return Response.status(200).entity("Appointment-Request received").build();
		}catch(Exception e){
			return Response.status(404).entity("Fehler beim Aufruf des Services 'appointment-request'!").build();
		}
	}
	
	@GET
	@Path("/data")
	public Response handleData() {
		String payLoad = "";
//		try{
			ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
			ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("data-request");
			
			if(instance.isEnded()){
				   List<HistoricVariableInstance> historicVariableInstance = processEngine
						   											.getHistoryService()
						   											.createHistoricVariableInstanceQuery()
						   											.processInstanceId(instance.getId())
						   											.list();
				   for(HistoricVariableInstance hvi: historicVariableInstance){
					   if(hvi.getName().equalsIgnoreCase("payload")){
						   payLoad = (String) hvi.getValue();
					   }
					   System.out.println(payLoad);
				   }
			}else{
				payLoad = (String) processEngine.getRuntimeService().getVariable(instance.getId(), "payLoad");
				System.out.println(payLoad);
			}
			return Response.status(200).entity(payLoad).build();
//		}catch(Exception e){
//			return Response.status(404).entity("Fehler beim Aufruf des Services 'data-request'!").build();
//		}
	}
}
