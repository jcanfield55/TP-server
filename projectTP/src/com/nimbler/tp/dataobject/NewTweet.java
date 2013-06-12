package com.nimbler.tp.dataobject;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class NewTweet implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 574289536887830676L;

	//
	private String id;

	@SerializedName("created_at")
	private String createdAt;

	private String text;
	@SerializedName("in_reply_to_user_id")
	private String inRelyToUserId;

	private TwitterUser user;

	@Override
	public String toString() {
		return "NewTweet [id=" + id + ", createdAt=" + createdAt + ", text="
				+ text + ", inRelyToUserId=" + inRelyToUserId + ", user="
				+ user + "]\n";
	}



	public String getId() {
		return id;
	}




	public TwitterUser getUser() {
		return user;
	}

	public String getUserSourceName() {
		return user!=null?user.getScreenName():"";
	}


	public void setUser(TwitterUser user) {
		this.user = user;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getCreatedAt() {
		return createdAt;
	}



	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}



	public String getText() {
		return text;
	}



	public void setText(String text) {
		this.text = text;
	}



	public String getInRelyToUserId() {
		return inRelyToUserId;
	}



	public void setInRelyToUserId(String inRelyToUserId) {
		this.inRelyToUserId = inRelyToUserId;
	}



	public static class TwitterUser implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3823677977988207213L;
		private String id;

		/**
		 * e.g Twitter API
		 */
		private String name;
		/**
		 * e.g twitterapi
		 */
		@SerializedName("screen_name")
		private String screenName;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getScreenName() {
			return screenName;
		}
		public void setScreenName(String screenName) {
			this.screenName = screenName;
		}
		@Override
		public String toString() {
			return "TwitterUser [id=" + id + ", name=" + name + ", screenName="
					+ screenName + "]";
		}
	}
}
