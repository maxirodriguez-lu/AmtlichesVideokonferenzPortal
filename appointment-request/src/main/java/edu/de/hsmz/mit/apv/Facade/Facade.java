package edu.de.hsmz.mit.apv.Facade;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;

@ProcessApplication( "Appointment Request" )
public class Facade extends ServletProcessApplication {

}
