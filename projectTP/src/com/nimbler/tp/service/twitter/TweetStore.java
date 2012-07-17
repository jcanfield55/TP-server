package com.nimbler.tp.service.twitter;

import java.util.LinkedList;
import java.util.List;

import com.nimbler.tp.dataobject.Tweet;
/**
 * 
 * @author suresh
 *
 */
public class TweetStore {

	private static TweetStore tweetStore = new TweetStore();
	private List<Tweet> tweet;
	private int ungentTweetsMaxSize = 10;	
	private LinkedList<Tweet> urgentTweets;

	private TweetStore() {
	}

	public static TweetStore getInstance(){
		return tweetStore;
	}

	public void setTweet(List<Tweet> tweet) {
		this.tweet = tweet;
	}

	public List<Tweet> getTweet() {
		return tweet;
	}

	public LinkedList<Tweet> getUrgentTweets() {
		return urgentTweets;
	}
	/**
	 * 
	 * @param urgentTweet
	 */
	public void addUrgentTweet(Tweet urgentTweet) {		
		if (this.urgentTweets==null) {
			this.urgentTweets = new LinkedList<Tweet>();
		}
		if (this.urgentTweets.size()==this.ungentTweetsMaxSize) {
			this.urgentTweets.removeFirst();
		}
		this.urgentTweets.add(urgentTweet); 
	}
	/**
	 * 
	 * @param timestamp
	 * @return
	 */
	public int getTweetCountAfterTime(long timestamp, long lastLegTime) {
		int count = 0;
		for (Tweet tweet: this.tweet) {
			if (tweet.getTime() > timestamp && tweet.getTime() > lastLegTime)
				count++;
			else
				break;
		}
		return count;
	}
}