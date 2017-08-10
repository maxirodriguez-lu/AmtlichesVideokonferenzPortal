package edu.de.hsmz.mit.apv.Data;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class PermissionHandler implements JavaDelegate{

	private final static Logger LOGGER = Logger.getLogger("DATA-REQUESTS");
	
	public void execute(DelegateExecution execution) throws Exception {
				
		LOGGER.info(">>> Starting Permission check!");

		if(!checkPermission()){
			throw new RuntimeException("Fehlende Berechtigung1");
		}
	}

	private boolean checkPermission() {
		return true;
	}

}
