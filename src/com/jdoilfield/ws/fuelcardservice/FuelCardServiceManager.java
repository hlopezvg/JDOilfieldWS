package com.jdoilfield.ws.fuelcardservice;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jdoilfield.operationalsystem.manager.FuelCardAuthManager;
import com.jdoilfield.ws.GeneralManager;
import com.jdoilfield.ws.fuelcardservice.dto.FuelCardServiceRequest;
import com.jdoilfield.ws.fuelcardservice.dto.FuelCardServiceResponse;

@WebService(endpointInterface="com.jdoilfield.ws.fuelcardservice.FuelCardServiceInterface")
public class FuelCardServiceManager implements GeneralManager, FuelCardServiceInterface {
    
	protected final Logger logger = LoggerFactory.getLogger(FuelCardServiceManager.class);
	private FuelCardAuthManager fuelCardManager;
	
	@Override
	public FuelCardServiceResponse processFuelCard(FuelCardServiceRequest request) {
		// TODO Auto-generated method stub      
		FuelCardServiceResponse response = new FuelCardServiceResponse();
		logger.info(" ------------- ---------------- ------------------ ------------------ ----------------");
        logger.info("Comienza la ejecucion del Servicio de Registro de Fuel Cards");
        //TODO add the value to Constants
        //response.setServiceVersion(Constants.VERSION_TICKET_SERVICE);
        response.setServiceVersion("FUEL CARD VERSION 1");
        
        try{
        	fuelCardManager.processFuelCard(request.getFuelCardMobile(),"" , 1);
        }catch (Exception e) {
			
		}
		return response;
	}

	@Override
	public FuelCardServiceResponse deleteFuelCard(FuelCardServiceRequest request) {
		FuelCardServiceResponse response = new FuelCardServiceResponse();
        logger.info(" ------------- ---------------- ------------------ ------------------ ----------------");
        logger.info("Comienza la ejecucion del Servicio de Borrado de Fuel Cards");
        //TODO add the value to Constants
        //response.setServiceVersion(Constants.VERSION_TICKET_SERVICE);
        response.setServiceVersion("FUEL CARD VERSION 2");
        
        try{
        	fuelCardManager.deleteFuelCard(request.getFuelCardMobile(),"" , 1);
        }catch (Exception e) {
			
		}
		return response;
	}
	@Override
	public FuelCardServiceResponse updateFuelCard(FuelCardServiceRequest request) {
		FuelCardServiceResponse response = new FuelCardServiceResponse();
		logger.info(" ------------- ---------------- ------------------ ------------------ ----------------");
        logger.info("Comienza la ejecucion del Servicio de Actualizacion de Fuel Cards");
        //TODO add the value to Constants
        //response.setServiceVersion(Constants.VERSION_TICKET_SERVICE);
        response.setServiceVersion("FUEL CARD VERSION 1");
        
        try{
        	fuelCardManager.updateFuelCard(request.getFuelCardMobile(),"" , 1);
        }catch (Exception e) {
			
		}
		return response;
	}
	
	public FuelCardAuthManager getFuelCardManager() {
		return fuelCardManager;
	}
	public void setFuelCardManager(FuelCardAuthManager fuelCardManager) {
		this.fuelCardManager = fuelCardManager;
	}
}
