package com.jdoilfield.ws.syncservice;

import com.jdoilfield.operationalsystem.NoSyncResultsException;
import com.jdoilfield.ws.GeneralManager;
import com.jdoilfield.ws.common.util.Constants;
import com.jdoilfield.ws.common.util.ErrorManager;
import com.jdoilfield.ws.syncservice.business.AirplaneSync;
import com.jdoilfield.ws.syncservice.business.AirportSync;
import com.jdoilfield.ws.syncservice.business.BusinessPartnerSync;
import com.jdoilfield.ws.syncservice.business.ProductSync;
import com.jdoilfield.ws.syncservice.business.RoleComponentSync;
import com.jdoilfield.ws.syncservice.business.SyncServiceValidator;
import com.jdoilfield.ws.syncservice.business.UserSync;
import com.jdoilfield.ws.syncservice.domain.tables.AirplaneTable;
import com.jdoilfield.ws.syncservice.domain.tables.AirportTable;
import com.jdoilfield.ws.syncservice.domain.tables.BusinessPartnerTable;
import com.jdoilfield.ws.syncservice.domain.tables.ProductTable;
import com.jdoilfield.ws.syncservice.domain.tables.RoleComponentTable;
import com.jdoilfield.ws.syncservice.domain.tables.UserTable;
import com.jdoilfield.ws.syncservice.dto.SyncServiceRequest;
import com.jdoilfield.ws.syncservice.dto.SyncServiceResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.jws.WebService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.NDC;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(endpointInterface = "com.jdoilfield.ws.syncservice.SyncServiceInterface")
public class SyncServiceManager implements GeneralManager, SyncServiceInterface {
	protected final Logger logger = LoggerFactory.getLogger(SyncServiceManager.class);
	private AirportSync airportSync;
	private AirplaneSync airplaneSync;
	private BusinessPartnerSync businessPartnerSync;
	private ProductSync productSync;
	private RoleComponentSync roleComponentSync;
	private UserSync userSync;
	private SyncServiceValidator syncServiceValidator;
	private static final String dateFormat = "yyyy/MM/dd HH:mm:ss";
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private StopWatch stopWatch;

	public SyncServiceResponse getSync(SyncServiceRequest request) {
		this.stopWatch = new Log4JStopWatch();
		SyncServiceResponse response = new SyncServiceResponse();

		NDC.push("p:" + request.getDeviceId() + " l:" + request.getLogin());
		this.logger.info("---> Comienza la ejecucion del Servicio de Sync");

		response.setServiceVersion(Constants.VERSION_SYNC_SERVICE);

		if (!(this.syncServiceValidator.validateFields(request))) {
			response.setErrorCode(Constants.DEVICE_INVALID);
			response.setErrorMessage(ErrorManager.getMessage(Constants.DEVICE_INVALID));
			this.stopWatch.stop("sync-val.fail",
					ErrorManager.getMessage(Constants.DEVICE_INVALID));
		} else {
			this.stopWatch.lap("sync-valrequest.success");
			this.logger.info("Verificando dispositivo: "
					+ request.getDeviceId());

			if (StringUtils.isNotBlank(request.getUserTableLastUpdate())) {
				try {
					this.logger.info("Buscando datos User");

					UserTable userTable = this.userSync.getUserTable(
							request.getUserTableLastUpdate(),request.getDeviceId());
					response.setUserTable(userTable);

					this.stopWatch.lap("sync-users.success");
				} catch (NoSyncResultsException e) {
					this.logger.error(ErrorManager.getMessage(Constants.NO_SYNC_RESULTS));

					response.setErrorCode(Constants.NO_SYNC_RESULTS);
					response.setErrorMessage(ErrorManager.getMessage(Constants.NO_SYNC_RESULTS));
					this.stopWatch.lap("sync-users.NoSyncResults");
				}

			} else if (this.syncServiceValidator.validateDeviceIdAndLogin(
					request.getLogin(), request.getDeviceId())) {
				this.stopWatch.lap("sync-valdevicelogin.success");

				if (StringUtils.isNotBlank(request.getAirplaneTableLastUpdate())) {
					this.logger.info("Buscando datos Airplane");

					AirplaneTable airplaneTable = this.airplaneSync.getAirplaneTable(
									request.getAirplaneTableLastUpdate(),
									request.getLogin(), request.getDeviceId());
					response.setAirplaneTable(airplaneTable);
					this.stopWatch.lap("sync-airplane.success");
				}

				try {
					if (StringUtils.isNotBlank(request
							.getAirportTableLastUpdate())) {
						this.logger.info("Buscando datos Airport");

						AirportTable airportTable = this.airportSync
								.getAirportTable(request
										.getAirportTableLastUpdate());
						response.setAirportTable(airportTable);
						this.stopWatch.lap("sync-airport.success");
					}
				} catch (NoSyncResultsException e) {
					this.logger.error(ErrorManager
							.getMessage(Constants.NO_SYNC_RESULTS));

					response.setErrorCode(Constants.NO_SYNC_RESULTS);
					response.setErrorMessage(ErrorManager
							.getMessage(Constants.NO_SYNC_RESULTS));
					this.stopWatch.lap("sync-airport.NoSyncResults");
				}

				if (StringUtils.isNotBlank(request
						.getBusinessPartnerTableLastUpdate())) {
					this.logger.info("Buscando datos businessPartner");

					BusinessPartnerTable businessPartnerTable = this.businessPartnerSync
							.getBusinessPartnerTable(
									request.getBusinessPartnerTableLastUpdate(),
									request.getLogin());
					response.setBusinessPartnerTable(businessPartnerTable);
					this.stopWatch.lap("sync-bpartner.success");
				}

				if (StringUtils.isNotBlank(request.getProductTableLastUpdate())) {
					this.logger.info("Buscando datos Product");

					ProductTable productTable = this.productSync
							.getProductTable(request
									.getProductTableLastUpdate());
					response.setProductTable(productTable);
					this.stopWatch.lap("sync-product.success");
				}

				try {
					if (StringUtils.isNotBlank(request.getRoleComponentTableLastUpdate())) {
						this.logger.info("Buscando datos RoleComponent");

						RoleComponentTable roleComponentTable = this.roleComponentSync.getRoleComponentTable(request
										.getRoleComponentTableLastUpdate());
						response.setRoleComponentTable(roleComponentTable);
						this.stopWatch.stop("sync-rolec.success");
					}
				} catch (NoSyncResultsException e) {
					this.logger.error(ErrorManager.getMessage(Constants.NO_SYNC_RESULTS));

					response.setErrorCode(Constants.NO_SYNC_RESULTS);
					response.setErrorMessage(ErrorManager.getMessage(Constants.NO_SYNC_RESULTS));
					this.stopWatch.stop("sync-rolec.NoSyncResults");
				}

				response.setUpdateTime(df.format(new Date()));
			} else {
				this.logger.error(ErrorManager.getMessage(Constants.DEVICE_NOT_FOUND));

				response.setErrorCode(Constants.DEVICE_NOT_FOUND);
				response.setErrorMessage(ErrorManager.getMessage(Constants.DEVICE_NOT_FOUND));
				this.stopWatch.stop("sync-valdevicelogin.fail",
						ErrorManager.getMessage(Constants.DEVICE_NOT_FOUND));
			}
		}

		this.logger.info("--> Fin de Sync");
		NDC.pop();
		return response;
	}

	public void setAirplaneSync(AirplaneSync airplaneSync) {
		this.airplaneSync = airplaneSync;
	}

	public void setAirportSync(AirportSync airportSync) {
		this.airportSync = airportSync;
	}

	public void setBusinessPartnerSync(BusinessPartnerSync businessPartnerSync) {
		this.businessPartnerSync = businessPartnerSync;
	}

	public void setProductSync(ProductSync productSync) {
		this.productSync = productSync;
	}

	public void setRoleComponentSync(RoleComponentSync roleComponentSync) {
		this.roleComponentSync = roleComponentSync;
	}

	public void setSyncServiceValidator(
			SyncServiceValidator syncServiceValidator) {
		this.syncServiceValidator = syncServiceValidator;
	}

	public void setUserSync(UserSync userSync) {
		this.userSync = userSync;
	}
}