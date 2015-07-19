package com.jdoilfield.ws.ticketservice.business;

import com.jdoilfield.ws.common.util.WsRequestValidator;
import com.jdoilfield.ws.ticketservice.domain.MobileTicket;
import com.jdoilfield.ws.ticketservice.dto.TicketServiceRequest;
import org.apache.commons.lang.StringUtils;

public class Validation extends WsRequestValidator
{
  public boolean validateFields(TicketServiceRequest request)
  {
    boolean retValue = true;

    if (StringUtils.isBlank(request.getDeviceId())) {
      retValue = false;
    } else {
      MobileTicket mobileTicket = request.getTicket();

      if ((mobileTicket == null) || (StringUtils.isBlank(mobileTicket.getTicketCode())) || 
        (mobileTicket.getTicketIdMobile() == null) || (mobileTicket.getTicketIdMobile().longValue() == 0L) || 
        (mobileTicket.getAirportId() == null) || (mobileTicket.getAirportId().longValue() == 0L) || 
        (StringUtils.isBlank(mobileTicket.getItemCode())) || (StringUtils.isBlank(mobileTicket.getItemName())) || 
        (StringUtils.isBlank(mobileTicket.getCardCodeClient())) || 
        (StringUtils.isBlank(mobileTicket.getCardNameClient())) || 
        (StringUtils.isBlank(mobileTicket.getCardCodeProvider())) || 
        (StringUtils.isBlank(mobileTicket.getCardNameProvider())) || (StringUtils.isBlank(mobileTicket.getUserId().toString())) || 
        (StringUtils.isBlank(mobileTicket.getNewAirplane())) || (StringUtils.isBlank(mobileTicket.getDateTime()))) {
        retValue = false;
      }
    }

    return retValue;
  }
}