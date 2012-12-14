package com.nimbler.tp.service.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nimbler.tp.dataobject.Tweet;
/**
 * 
 * @author nIKUNJ
 *
 */
public class TweetStore {

	private static TweetStore tweetStore = new TweetStore();
	/**
	 * Contains tweets for specific agency.
	 */
	private Map<Integer, List<Tweet>> agencyToAdvisoriesMap = new HashMap<Integer, List<Tweet>>();
	/**
	 * Contains urgent advisories for specific agency.
	 */
	private Map<Integer, LinkedList<Tweet>> agencyToUrgentAdvisoriesMap = new HashMap<Integer, LinkedList<Tweet>>();
	private int ungentTweetsMaxSize = 10;

	//	private List<Tweet> tweet = new  ArrayList<Tweet>();
	//	private LinkedList<Tweet> urgentTweets;

	private TweetStore() {
	}
	/**
	 * 
	 * @return
	 */
	public static TweetStore getInstance() {
		return tweetStore;
	}
	/**
	 * 
	 * @param tweets
	 * @param agency
	 */
	public void setTweet(List<Tweet> tweets, int agency) {
		agencyToAdvisoriesMap.put(agency, tweets); 
	}
	/**
	 * Get tweets for specific agency.
	 * @param agencyType
	 * @return
	 */
	public List<Tweet> getTweets(int agencyType) {
		return agencyToAdvisoriesMap.get(agencyType); 
	}
	/**
	 * 
	 * @param agencyTypes - array of ordinal values of AGENCY_TYPE enum
	 * @return
	 */
	public List<Tweet> getTweets(int[] agencyTypes) {
		List<Tweet> allTweets = new ArrayList<Tweet>();
		for (int agencyType: agencyTypes) {
			List<Tweet> agencyTweets = agencyToAdvisoriesMap.get(agencyType);
			if (agencyTweets!=null && agencyTweets.size()>0)
				allTweets.addAll(agencyTweets);			 
		}
		return allTweets;
	}
	/**
	 * 
	 * @param agencyType
	 * @return
	 */
	public LinkedList<Tweet> getUrgentTweets(int agencyType) {		
		return agencyToUrgentAdvisoriesMap.get(agencyType);
	}
	/**
	 * 
	 * @param agencyIds
	 * @return
	 */
	public LinkedList<Tweet> getUrgentTweets(int[] agencyIds) {		
		LinkedList<Tweet> allTweets = new LinkedList<Tweet>();
		for (int agencyId: agencyIds) {
			List<Tweet> agencyTweets = agencyToUrgentAdvisoriesMap.get(agencyId);
			if (agencyTweets!=null && agencyTweets.size()>0)
				allTweets.addAll(agencyTweets);			 
		}
		return allTweets;
	}
	/**
	 * 
	 * @param urgentTweet
	 * @param appType
	 */
	public void addUrgentTweet(Tweet urgentTweet, int agencyType) {
		synchronized (agencyToUrgentAdvisoriesMap) {
			LinkedList<Tweet> tweets = this.agencyToUrgentAdvisoriesMap.get(agencyType);
			if (tweets == null) {
				tweets = new LinkedList<Tweet>();
				agencyToUrgentAdvisoriesMap.put(agencyType, tweets); 
			} else {
				if (tweets.size()==this.ungentTweetsMaxSize)
					tweets.removeFirst();
			}
			tweets.add(urgentTweet);
		}
	}
	/**
	 * 
	 * @param timestamp
	 * @return
	 */
	public int getTweetCountAfterTime(long timestamp, long lastLegTime, int agency) {
		int count = 0;
		if (this.agencyToAdvisoriesMap.get(agency) != null) {
			for (Tweet tweet: this.agencyToAdvisoriesMap.get(agency)) {
				if (tweet.getTime() > timestamp && tweet.getTime() > lastLegTime)
					count++;
				else
					break;
			}
		}
		return count;
	}
	/**
	 * 
	 * @param timestamp
	 * @param lastLegTime
	 * @return
	 */
	public List<String> getTweetsAfterTime(long timestamp, long lastLegTime, int agency) {
		List<String> tweets = new ArrayList<String>();
		if (this.agencyToAdvisoriesMap.get(agency) != null) {
			for (Tweet tweet: this.agencyToAdvisoriesMap.get(agency)) {
				if (tweet.getTime() > timestamp && tweet.getTime() > lastLegTime)
					tweets.add(tweet.getTweet());
				else
					break;
			}
		}
		return tweets;
	}
	/**
	 * 
	 */
	public void clearUrgentAdvisories() {
		synchronized (agencyToUrgentAdvisoriesMap) {
			agencyToUrgentAdvisoriesMap.clear();
		}
	}
	/**
	 * 
	 * @return
	 */
	public List<Tweet> getAllTweets() {
		return new ArrayList(agencyToAdvisoriesMap.values());
	}
	/*public void setTweet(List<Tweet> tweet) {
	this.tweet = tweet;
}*/

	/*public List<Tweet> getTweet() {
	return tweet;
}*/
	/**
	 * 
	 * @param urgentTweet
	 */
	/*public void addUrgentTweet(Tweet urgentTweet) {
		if (this.urgentTweets==null) {
			this.urgentTweets = new LinkedList<Tweet>();
		}
		synchronized (urgentTweets) {
			if (this.urgentTweets.size()==this.ungentTweetsMaxSize) {
				this.urgentTweets.removeFirst();
			}		
			this.urgentTweets.add(urgentTweet);
		}
	}*/
	/*	public LinkedList<Tweet> getUrgentTweets() {
	return urgentTweets;
}*/
}