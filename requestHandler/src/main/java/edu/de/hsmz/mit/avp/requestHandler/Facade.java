package edu.de.hsmz.mit.avp.requestHandler;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/request")
public class Facade {

	@POST
	@Path("/appointment")
	public Response handleAppointment() {
		return AppointmentHandler.handleRequest();
	}
	
	@GET
	@Path("/getData/{objectType}")
	public Response handleRead(@PathParam("objectType") final String objectType, @Context UriInfo info) {		
		return DataHandler.handleDataReadRequest(objectType, info);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/setData/{objectType}")
	public Response handleWrite(@PathParam("objectType") final String objectType, @Context UriInfo info, String payLoad){
		return DataHandler.handleDataWriteRequest(objectType, info, payLoad);	
	}
	
}
