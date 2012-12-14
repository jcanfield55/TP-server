/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.gtfs.GraphAcceptanceTest;

@Path("/test/")
public class TestService {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/test/")
	public String firstTest() {
		return "Test service";
	}
	/*@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/apn/")
	public String testApn(@QueryParam("token") String token,@QueryParam("msg") String msg,@QueryParam("badge") String badge) {
		APNService service = BeanUtil.getApnService();
		service.push(token, msg,NumberUtils.toInt(badge),null,true);
		System.out.println("APN sent.....");
		return "sucess";
	}*/
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/graph/")
	public String testGtaph() {
		GraphAcceptanceTest gat = (GraphAcceptanceTest) TPApplicationContext.getBeanByName("gat");
		gat.doTest();
		return "sucess";
	}
}