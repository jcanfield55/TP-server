/*
 * @author nirmal
 */
package com.nimbler.tp.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class WSAuthentication implements Authentication {

	private static final long serialVersionUID = -1484204809314498494L;

	private String username;
	private String passwordDigest;
	private boolean authenticated;
	private UserDetails userDetails;

	/**
	 * The Class UserDetails.
	 *
	 * @author nirmal
	 */
	class UserDetails{
		private String username;
		private String password;
		private UserDetails(String username, String password) {
			super();
			this.username = username;
			this.password = password;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	}

	public WSAuthentication(String username, String passwordDigest) {
		this.username = username;
		this.passwordDigest = passwordDigest;
		userDetails = new UserDetails(username,passwordDigest);
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
		return authorities;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getDetails() {
		return userDetails;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
		this.authenticated = authenticated;
	}

	@Override
	public String getName() {
		return username;
	}


}
