package com.nimbler.tp.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class WSAuthEntryPoint implements AuthenticationEntryPoint {

	private String realm;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {

		response.addHeader("WWW-Authenticate", "WSSE realm=\"" + realm + "\", profile=\"UsernameToken\"");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());		
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

}
