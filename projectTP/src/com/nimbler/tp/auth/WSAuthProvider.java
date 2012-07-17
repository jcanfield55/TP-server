package com.nimbler.tp.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.nimbler.tp.auth.WSAuthentication.UserDetails;


public class WSAuthProvider implements AuthenticationProvider {

	private MessageDigest sha1;
	private HashMap<String, String> users;

	WSAuthProvider() {
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public Authentication authenticate(Authentication auth)
			throws AuthenticationException {

		String username = auth.getName();
		WSAuthentication.UserDetails userDetails = (UserDetails) auth.getDetails();
		String password  = users.get(username);
		if(password==null)
			throw new BadCredentialsException("Invalid User");
		if(!userDetails.getPassword().equals(password))
			throw new BadCredentialsException("Invalid Password");
		auth.setAuthenticated(true);
		return auth;
	}

	@Override
	public boolean supports(Class<? extends Object> cls) {
		return WSAuthentication.class.isAssignableFrom(cls);
	}

	public void setUsers(List<String> userStrings) {
		users = new HashMap<String, String>();
		for (String userSpec : userStrings) {
			String[] parts = userSpec.split("=");
			users.put(parts[0], parts[1]);
		}
	}
}
