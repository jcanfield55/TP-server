/*
 * @author nirmal
 */
package com.nimbler.tp.service.advisories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.nimbler.tp.dataobject.Tweet;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;


/**
 * The Class BartAlertCriteria.
 *
 * @author nirmal
 */
public class BartAlertCriteria {
	private String patternTimeMatch = "(\\d{0,2}(?:to|\\s|-){0,4}\\d{1,2}[- ]{0,3}minute delay)";
	private String majorDelayString = "major delay";
	private Integer majorDelay = 20;
	private String patternDigit = "[0-9]+";
	//	private Long pushIntervalPerFreqInMillSec = 1200000L; //20*60*1000;
	private Long pushIntervalPerFreqInMillSec = 240000L; //20*60*1000;
	private String tweetTimeQuery = "this."+AGENCY_TYPE.BART.getPushTimeColumnName()+" < ( %s - (%s*(this.numberOfAlert-1)))";

	private SortedMap<Integer,Range<Integer>> alertToDelayMap = null; 
	@PostConstruct
	private void init() {		
		alertToDelayMap = new TreeMap<Integer, Range<Integer>>(); 
		alertToDelayMap.put(10,Ranges.atLeast(17));
		alertToDelayMap.put(9,Ranges.atLeast(17));
		alertToDelayMap.put(8,Ranges.atLeast(17));
		alertToDelayMap.put(7,Ranges.atLeast(17));
		alertToDelayMap.put(6,Ranges.atLeast(17));
		alertToDelayMap.put(5,Ranges.atLeast(17));
		alertToDelayMap.put(4,Ranges.atLeast(15));
		alertToDelayMap.put(3,Ranges.atLeast(12));
		alertToDelayMap.put(2,Ranges.atLeast(10));
	}
	public BartAlertCriteria() {

	}

	/**
	 * Checks if is major detay.
	 *
	 * @param tweet the tweet
	 * @return true, if is major detay
	 */
	public boolean isMajorDelay(String  tweet){
		return StringUtils.containsIgnoreCase(tweet, majorDelayString);		
	}
	/**
	 * 1. Tweet Time <br />
	 * 2. Interval constant i.e 20 min
	 *
	 * @return the tweet time query
	 */
	public String getTweetTimeQuery() {
		return String.format(tweetTimeQuery, "%s",pushIntervalPerFreqInMillSec+"");
	}

	/**
	 * Gets the delay time in tweet.
	 *
	 * @param tweet the tweet
	 * @return the delay time in tweet
	 */
	public Integer  getDelayTimeInTweet(String  tweet){
		tweet = tweet.toLowerCase();
		Pattern pattern = Pattern.compile(patternTimeMatch);
		Matcher matcher=  pattern.matcher(tweet);		
		if(matcher.find()){
			String matchingString = matcher.group(1);
			pattern = Pattern.compile(patternDigit);
			matcher=  pattern.matcher(matchingString);
			int count=0;
			int total=0;			
			while(matcher.find()){
				String strVal =  matcher.group();
				if(ComUtils.isEmptyString(strVal))
					continue;
				int val = NumberUtils.toInt(strVal);
				total = total+val;
				count++;
			}
			if(count>0)
				return total/count;
		}
		return null;
	}


	public String getPatternTimeMatch() {
		return patternTimeMatch;
	}

	public void setPatternTimeMatch(String patternTimeMatch) {
		this.patternTimeMatch = patternTimeMatch;
	}

	public String getMajorDelayString() {
		return majorDelayString;
	}

	public void setMajorDelayString(String majorDelayString) {
		this.majorDelayString = majorDelayString;
	}

	public String getPatternDigit() {
		return patternDigit;
	}

	public void setPatternDigit(String patternDigit) {
		this.patternDigit = patternDigit;
	}

	public Long getPushIntervalPerFreqInMillSec() {
		return pushIntervalPerFreqInMillSec;
	}

	public void setPushIntervalPerFreqInMillSec(Long pushIntervalPerFreqInMillSec) {
		this.pushIntervalPerFreqInMillSec = pushIntervalPerFreqInMillSec;
	}



	public void setTweetTimeQuery(String tweetTimeQuery) {
		this.tweetTimeQuery = tweetTimeQuery;
	}

	/**
	 * Gets the elible alert count.
	 *
	 * @param tweet the tweet
	 * @return the elible alert count
	 */
	public Object[] getElibleAlertCount(Tweet tweet) {
		Set<Integer> lst = new HashSet<Integer>();
		lst.add(1);
		boolean isMajorDelay = isMajorDelay(tweet.getTweet());
		Integer delay = isMajorDelay?majorDelay:getDelayTimeInTweet(tweet.getTweet());
		if(delay!=null){
			for (Map.Entry<Integer, Range<Integer>> entry : alertToDelayMap.entrySet()) {
				Integer alert = entry.getKey();
				Range<Integer> delayRange = entry.getValue();
				if(delayRange.apply(delay)){
					lst.add(alert);
				}
			}
		}
		return lst.toArray();
	}
	public SortedMap<Integer, Range<Integer>> getAlertToDelayMap() {
		return alertToDelayMap;
	}
	public void setAlertToDelayMap(
			SortedMap<Integer, Range<Integer>> alertToDelayMap) {
		this.alertToDelayMap = alertToDelayMap;
	}

	public Integer getMajorDelay() {
		return majorDelay;
	}
	public void setMajorDelay(Integer majorDelay) {
		this.majorDelay = majorDelay;
	}	
	public static void main(String[] args) {
		List<String> tweets = new ArrayList<String>();
		tweets.add("There is a 20-25 minute delay in the SFO and Millbrae directions due to an equipment problem on the track.");
		tweets.add("There is a 20 minute delay in the SFO and Millbrae directions due to an equipment problem on the track.");
		tweets.add("There is a 10-12 minute delay in the SFO and Millbrae directions due to an equipment problem on the track.");
		tweets.add("There is a 12-15 minute delay in the SFO and Millbrae directions due to an equipment problem on the track.");
		tweets.add("There is a 15-20 minute delay in the SFO and Millbrae directions due to an equipment problem on the track.");
		tweets.add("There is a 15-17 minute delay in the SFO and Millbrae directions due to an equipment problem on the track.");
		tweets.add("There is a major delay in the SFO and Millbrae directions due to an equipment problem on the track.");
		BartAlertCriteria criteria = new BartAlertCriteria();
		criteria.init();
		for (String tweet : tweets) {
			System.out.println(ReflectionToStringBuilder.toString(criteria.getElibleAlertCount(new Tweet(tweet, 0L, false)),
					ToStringStyle.SHORT_PREFIX_STYLE));			
		}
	}
}
