/*
 * @author nirmal
 */
package com.nimbler.tp.service.advisories;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.dataobject.DefaultTweet.TwitterResponse;
import com.nimbler.tp.dataobject.NewTweet;
import com.nimbler.tp.dataobject.Tweet;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.TpException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;

public class TwitterSearchManager {
	@Autowired 
	private LoggingService logger;

	private String loggerName = "com.nimbler.tp.service.advisories.AdvisoriesService";
	private String tweetUrl = "https://api.twitter.com/1.1/search/tweets.json";
	private String twitterDateFormat = "E MMM dd HH:mm:ss Z yyyy";
	private String tweetsToFetch = "100";
	private String twitterBearer = "AAAAAAAAAAAAAAAAAAAAAEgpRQAAAAAAvwvH0xdKsXLskK5AEs1TNpgf3Y8%3DLRpUtnTFqzfbhmkdtKYfAA6MaHcrClZcb2R8EB1s";
	private String twitterUserAgent = "Nimbler App";
	private int validTweetHours = 6;
	private Client twitterClient = null;

	@PostConstruct
	public void init() {
		twitterClient = Client.create();
		twitterClient.addFilter(new GZIPContentEncodingFilter(false));
	}

	/**
	 * Fetch tweets.
	 *
	 * @param tweetSources the tweet sources
	 * @param agencyTweetSourceIconMap the agency tweet source icon map
	 * @return the list
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws TpException the tp exception
	 * @throws ParseException the parse exception
	 */
	public List<Tweet> fetchTweets(String[] tweetSources, Map<String, String> agencyTweetSourceIconMap) throws UnsupportedEncodingException, TpException, ParseException {
		List<String> list = new ArrayList<String>();
		for (String source: tweetSources){
			list.add("from:"+source.trim());
		}
		String queryParam = StringUtils.join(list,"+OR+")+ " since:" + ComUtils.getFormatedDate("yyyy-MM-dd");
		String response = getTwitterResponse(queryParam);

		List<Tweet> tweetList = new ArrayList<Tweet>();
		TwitterResponse twitterResponse = (TwitterResponse) JSONUtil.getObjectFromJson(response, TwitterResponse.class);
		if(twitterResponse!=null && !ComUtils.isEmptyList(twitterResponse.getStatuses())){
			for (NewTweet dt : twitterResponse.getStatuses()) {
				if(!ComUtils.isEmptyString(dt.getInRelyToUserId()))
					continue;
				String tweetdate = dt.getCreatedAt();
				SimpleDateFormat dateFormat = new  SimpleDateFormat(twitterDateFormat);
				long tweetTime	= dateFormat.parse(tweetdate).getTime();
				boolean validTweet = isValidTweet(tweetTime);
				if (!validTweet)
					continue;
				Tweet tweet = new Tweet();
				tweet.setTweetTime(tweetdate);
				tweet.setTime(tweetTime);
				tweet.setTweet("@"+dt.getUserSourceName()+":"+dt.getText());
				tweet.setSource(agencyTweetSourceIconMap.get(StringUtils.lowerCase(dt.getUserSourceName())));
				tweetList.add(tweet);
			}
		}
		return tweetList;
	}


	/**
	 * Gets the twitter response.
	 *
	 * @param queryParameter the query parameter
	 * @return the twitter response
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws TpException the tp exception
	 */
	private String getTwitterResponse(String queryParameter) throws  UnsupportedEncodingException, TpException {
		String reqUrl = new StringBuffer(tweetUrl).
				append("?q=").append(URLEncoder.encode(queryParameter, "UTF-8"))
				.append("&count=").append(tweetsToFetch).toString();

		WebResource resource  = twitterClient.resource(reqUrl);
		ClientResponse cr = resource.
				header("User-Agent", twitterUserAgent).
				header("Authorization", "Bearer "+twitterBearer).
				header("Accept-Encoding", "gzip").
				get(ClientResponse.class);
		if(cr.getStatus()!=200)
			throw new TpException("server returned status code "+cr.getStatus()+" for "+queryParameter);
		return cr.getEntity(String.class);
	}
	/**
	 * 
	 * @param createdDate
	 * @return
	 */
	private boolean isValidTweet(Long createdDate) {
		long oldtimeLimit = System.currentTimeMillis()- (validTweetHours * DateUtils.MILLIS_PER_HOUR);		
		return (createdDate > oldtimeLimit);
	}

	public LoggingService getLogger() {
		return logger;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getTweetUrl() {
		return tweetUrl;
	}

	public void setTweetUrl(String tweetUrl) {
		this.tweetUrl = tweetUrl;
	}

	public String getTwitterDateFormat() {
		return twitterDateFormat;
	}

	public void setTwitterDateFormat(String twitterDateFormat) {
		this.twitterDateFormat = twitterDateFormat;
	}

	public String getTweetsToFetch() {
		return tweetsToFetch;
	}

	public void setTweetsToFetch(String tweetsToFetch) {
		this.tweetsToFetch = tweetsToFetch;
	}

	public String getTwitterBearer() {
		return twitterBearer;
	}

	public void setTwitterBearer(String twitterBearer) {
		this.twitterBearer = twitterBearer;
	}

	public String getTwitterUserAgent() {
		return twitterUserAgent;
	}

	public void setTwitterUserAgent(String twitterUserAgent) {
		this.twitterUserAgent = twitterUserAgent;
	}

	public int getValidTweetHours() {
		return validTweetHours;
	}

	public void setValidTweetHours(int validTweetHours) {
		this.validTweetHours = validTweetHours;
	}

	public Client getTwitterClient() {
		return twitterClient;
	}

	public void setTwitterClient(Client twitterClient) {
		this.twitterClient = twitterClient;
	}

}
