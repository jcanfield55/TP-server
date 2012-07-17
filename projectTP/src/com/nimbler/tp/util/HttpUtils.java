/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
/**
 * 
 * @author nirmal
 *
 */
public class HttpUtils {

	/**
	 * Generate plan from otp.
	 *
	 * @param url the url
	 * @return the string
	 * @throws MalformedURLException the malformed url exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TpException the tp exception
	 */
	public static String getHttpResponse(String url) throws TpException {
		StringBuilder sb = new StringBuilder();
		HttpURLConnection connection = null; 
		try {
			connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setDoOutput(true);
			connection.addRequestProperty("Accept", "Application/json");
			int responseCode = connection.getResponseCode();
			if(responseCode != 200)
				throw new TpException("Server returned http response code "+responseCode);
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while((line = br.readLine()) !=null)
				sb.append(line);
		} catch (UnknownHostException e) {
			throw new TpException("UnknownHost: "+e.getMessage());
		} catch (MalformedURLException e) {
			throw new TpException("MalformedURL: "+e.getMessage());
		}catch (IOException e) {
			throw new TpException(e.getMessage());
		}finally{
			if(connection !=null){
				connection.disconnect();
			}
		}
		return sb.toString();
	}

	/**
	 * Gets the request parameters.
	 *
	 * @param httpRequest the http request
	 * @return the request parameters
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getRequestParameters(HttpServletRequest httpRequest) {
		Map<String,String> reqMap = new HashMap<String, String>();
		Enumeration<String> enumeration = httpRequest.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String key = (String) enumeration.nextElement();
			reqMap.put(key, httpRequest.getParameter(key));
		}
		return reqMap;
	}
}
