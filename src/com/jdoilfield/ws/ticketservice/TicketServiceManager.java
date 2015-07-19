package com.jdoilfield.ws.ticketservice;

import java.text.SimpleDateFormat;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jdoilfield.operationalsystem.EntidadExistenteException;
import com.jdoilfield.operationalsystem.domain.local.Airport;
import com.jdoilfield.operationalsystem.domain.local.Ticket;
import com.jdoilfield.operationalsystem.domain.local.User;
import com.jdoilfield.operationalsystem.manager.AirportManager;
import com.jdoilfield.operationalsystem.manager.TicketAuthManager;
import com.jdoilfield.operationalsystem.manager.UserManager;
import com.jdoilfield.operationalsystem.mobilemessage.MDTException;
import com.jdoilfield.ws.GeneralManager;
import com.jdoilfield.ws.common.util.Constants;
import com.jdoilfield.ws.common.util.ErrorManager;
import com.jdoilfield.ws.ticketservice.business.Validation;
import com.jdoilfield.ws.ticketservice.dto.TicketOverdraftRequest;
import com.jdoilfield.ws.ticketservice.dto.TicketServiceRequest;
import com.jdoilfield.ws.ticketservice.dto.TicketServiceResponse;
import com.jdoilfield.ws.ticketservice.impl.PushSenderThread;
import com.pranical.commons.exceptions.LogicException;


// Referenced classes of package com.jdoilfield.ws.ticketservice:
//            TicketServiceInterface

@WebService(endpointInterface="com.jdoilfield.ws.ticketservice.TicketServiceInterface")
public class TicketServiceManager
    implements GeneralManager, TicketServiceInterface
{

    public TicketServiceManager()
    {
    }

    public TicketServiceResponse processTicket(TicketServiceRequest request)
    {
        TicketServiceResponse response = new TicketServiceResponse();
        logger.info(" ------------- ");
        logger.info("Comienza la ejecucion del Servicio de Registro de Boletas");
        response.setServiceVersion(Constants.VERSION_TICKET_SERVICE);
        if(!validation.validateFields(request))
        {
            response.setErrorCode(Constants.DEVICE_INVALID);
            response.setErrorMessage(ErrorManager.getMessage(Constants.DEVICE_INVALID));

        } else
        {
            logger.info((new StringBuilder("Verificando dispositivo: ")).append(request.getDeviceId()).toString());
            if(validation.validateDeviceId(request.getDeviceId()))
            {
                try
                {
                    Integer ticketId = ticketManager.processTicket(request.getTicket(), request.getDeviceId(), request.getId());
                    Ticket ticket = (Ticket)ticketManager.findById(Ticket.class, ticketId);
                    if(ticket != null)
                    {
                        response.setOverdraft(ticket.getOverdraftString());
                        response.setStatus(String.valueOf(ticket.getStatus()));
                        response.setTicketId(ticket.getId());
                        if("Y".equalsIgnoreCase(ticket.getOverdraftString()))
                            sendPush(ticket);
                        response.setErrorCode(Constants.SERVICE_OK);
                        response.setErrorMessage(ErrorManager.getMessage(Constants.SERVICE_OK));
                    } else
                    {
                        response.setErrorCode(Constants.TICKET_NOT_FOUND);
                        response.setErrorMessage(ErrorManager.getMessage(Constants.TICKET_NOT_FOUND));
                    }
                }
                catch(LogicException e)
                {
                    response.setErrorCode(Constants.COULD_NOT_READ_BALANCE);
                    response.setErrorMessage(ErrorManager.getMessage(Constants.COULD_NOT_READ_BALANCE));
                }
                catch(EntidadExistenteException e)
                {
                    response.setErrorCode(Constants.TICKET_ALREADY_EXIST);
                    response.setErrorMessage(ErrorManager.getMessage(Constants.TICKET_ALREADY_EXIST));
                }
                catch(MDTException e)
                {
                    response.setErrorCode(Constants.NOT_SENDED_PUSH);
                    response.setErrorMessage(ErrorManager.getMessage(Constants.NOT_SENDED_PUSH));
                }
            } else
            {
                logger.error(ErrorManager.getMessage(Constants.DEVICE_NOT_FOUND));
                response.setErrorCode(Constants.DEVICE_NOT_FOUND);
                response.setErrorMessage(ErrorManager.getMessage(Constants.DEVICE_NOT_FOUND));
            }
        }
        return response;
    }

    public void sendPush(Ticket ticket)
        throws MDTException
    {
        Airport airport = null;
        User user = null;
        SimpleDateFormat df = new SimpleDateFormat(Constants.dateFormat);
        logger.debug("Aeropuerto:{} User:{}", Integer.valueOf(ticket.getAirportId()), ticket.getIdUser());
        airport = (Airport)airportManager.findById(Airport.class, Integer.valueOf(ticket.getAirportId()));
        user = (User)userManager.findById(User.class, ticket.getIdUser());
        String deviceId = user.getSupervisorUser().getDeviceId();
        String ticketId = (new StringBuilder()).append(ticket.getId()).toString();
        String ticketCode = ticket.getTicketCode();
        String cardCodeProvider = ticket.getProviderCode();
        String airportCode = airport.getCode();
        String itemCode = ticket.getProductCode();
        String cardCodeClient = ticket.getClientCode();
        String dateTime = df.format(ticket.getDatetime());
        String quantityLts = (new StringBuilder()).append(ticket.getQuantityLts()).toString();
        String quantityGal = (new StringBuilder()).append(ticket.getQuantityGal()).toString();
        String airplaneCode = ticket.getAirplaneCode();
        String notes = ticket.getNotes();
        String overdraft = (new StringBuilder()).append(ticket.getOverdraft()).toString();
        String newAirplane = ticket.getNewAirplane();
        String releasedBy = ticket.getReleasedBy();
        String newAirplaneCode = ticket.getNewAirplaneCode();
        String status = (new StringBuilder()).append(ticket.getStatus()).toString();
        String idUser = (new StringBuilder()).append(ticket.getIdUser()).toString();
        TicketOverdraftRequest msg = new TicketOverdraftRequest(ticketId, ticketCode, cardCodeProvider, airportCode, itemCode, cardCodeClient, 
        		dateTime, quantityLts, quantityGal, airplaneCode, notes, overdraft, newAirplane, releasedBy, newAirplaneCode, status, idUser,  deviceId);
        (new PushSenderThread((new StringBuilder("PushProcess_")).append(ticketId).toString(), msg)).start();
    }

    public void setTicketManager(TicketAuthManager ticketManager)
    {
        this.ticketManager = ticketManager;
    }

    public void setAirportManager(AirportManager airportManager)
    {
        this.airportManager = airportManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setValidation(Validation validation)
    {
        this.validation = validation;
    }

    protected final Logger logger = LoggerFactory.getLogger(TicketServiceManager.class);
    private TicketAuthManager ticketManager;
    private AirportManager airportManager;
    private UserManager userManager;
    private Validation validation;
}