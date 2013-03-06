package com.nimbler.tp.dataobject;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author nirmal
 *
 */
public class DefaultTweet implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2431537776908922638L;
	private String created_at;
	private String text;
	private String from_user;
	private String to_user_name;
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getFrom_user() {
		return from_user;
	}
	public void setFrom_user(String from_user) {
		this.from_user = from_user;
	}
	public String getTo_user_name() {
		return to_user_name;
	}
	public void setTo_user_name(String to_user_name) {
		this.to_user_name = to_user_name;
	}

	public class TwitterResponse implements Serializable{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 185116374571817242L;
		private List<DefaultTweet> results;

		public List<DefaultTweet> getResults() {
			return results;
		}

		public void setResults(List<DefaultTweet> results) {
			this.results = results;
		}
	}

}
