package com.apprika.otp.ws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.apprika.otp.service.LoggingService;

@Path("/test/")
public class TestService {
	Logger logger = LoggingService.getLoggingService(TestService.class.getName());

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/test/")
	public String firstTest() {
		return "Test service";
	}
}