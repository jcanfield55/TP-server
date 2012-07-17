package com.nimbler.tp.auth;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

public class WSAuthFilter extends GenericFilterBean {

	private String realmName;
	private AuthenticationManager authenticationManager;
	private AuthenticationEntryPoint authenticationEntryPoint;
	private boolean ignoreFailure;

	@Override
	public void afterPropertiesSet() {
		Assert.notNull(this.authenticationManager,
				"An AuthenticationManager is required");

		if (!isIgnoreFailure()) {
			Assert.notNull(this.authenticationEntryPoint,
					"An AuthenticationEntryPoint is required");
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		Authentication authResult;

		String authHeader = request.getHeader("Authorization");
		if (authHeader != null
				&& authHeader.equals("WSSE profile=\"UsernameToken\"")) {

			try {
				Authentication authRequest = makeAuthRequest(request);
				authResult = authenticationManager.authenticate(authRequest);
			} catch (AuthenticationException failed) {
				// Authentication failed
				SecurityContextHolder.getContext().setAuthentication(null);
				onUnsuccessfulAuthentication(request, response, failed);
				if (ignoreFailure) {
					chain.doFilter(request, response);
				} else {
					authenticationEntryPoint
					.commence(request, response, failed);
				}

				return;
			}

			SecurityContextHolder.getContext().setAuthentication(authResult);
			onSuccessfulAuthentication(request, response, authResult);
		}

		chain.doFilter(request, response);
	}

	private Authentication makeAuthRequest(HttpServletRequest request) {
		String wsseHeader = request.getHeader("X-WSSE");
		if (wsseHeader == null) {
			throw new BadCredentialsException("No wsse header");
		}

		String[] tokenParts = wsseHeader.split(", ");
		if (tokenParts.length != 2) {
			throw new BadCredentialsException("Wrong number of params (expected 2 got " + tokenParts.length + ")");
		} 
		String[] method_username = tokenParts[0].split(" ");
		if (method_username.length != 2
				|| !method_username[0].equals("UsernameToken")) {
			throw new BadCredentialsException("Not UsernameToken");
		}

		HashMap<String, String> params = parse_params(method_username[1],
				tokenParts[1]);
		if (params == null) {
			throw new BadCredentialsException("Invalid params");
		}

		return new WSAuthentication(params.get("Username"), params.get("PasswordDigest"));

	}

	protected boolean isIgnoreFailure() {
		return ignoreFailure;
	}

	public void setIgnoreFailure(boolean ignoreFailure) {
		this.ignoreFailure = ignoreFailure;
	}

	protected void onSuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authResult)
					throws IOException {
	}

	protected void onUnsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException failed)
					throws IOException {
	}

	protected AuthenticationEntryPoint getAuthenticationEntryPoint() {
		return authenticationEntryPoint;
	}

	public void setAuthenticationEntryPoint(
			AuthenticationEntryPoint authenticationEntryPoint) {
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	private HashMap<String, String> parse_params(String... strings) {
		HashMap<String, String> param_map = new HashMap<String, String>();
		for (String kv : strings) {
			String[] key_value = kv.split("=", 2);
			if (key_value.length != 2) {
				return null;
			}
			param_map.put(key_value[0], key_value[1].replace("\"", ""));
		}
		return param_map;
	}

	public void setRealmName(String realmName) {
		this.realmName = realmName;
	}

	public String getRealmName() {
		return realmName;
	}

	public void setAuthenticationManager(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

}
