package edu.de.hsmz.mit.apv.Data;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class TimeOutHandler implements JavaDelegate{

	private final static Logger LOGGER = Logger.getLogger("DATA-REQUESTS");
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		LOGGER.info(">>> Error from data-request caught!");

		JSONObject payLoad = new JSONObject();
			JSONObject error = new JSONObject();
			error.put("id", execution.getVariable("dataRequestError_Code"));
			error.put("message", execution.getVariable("dataRequestError_Message"));
		payLoad.put("error", error);
			
		execution.setVariable("payload", payLoad.toString());
	}

}
