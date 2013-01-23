/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.GtfsUtils;


/**
 * The Class GtfsMetaExtractor.
 *
 * @author nirmal
 */
public class GtfsMetaExtractor implements Runnable{
	private static final int MIN_SIZE_TO_SORT = 100;

	public SimpleDateFormat gtfsDateFormat = new SimpleDateFormat("yyyyMMdd");

	private GtfsBundle bundle;
	private LoggingService logger;
	private String loggerName;
	public GtfsMetaExtractor(GtfsBundle bundle, LoggingService logger,
			String loggerName) {
		this.bundle = bundle;
		this.logger = logger;
		this.loggerName = loggerName;
	}

	@Override
	public void run() {
		File gtfsFile = null;
		try {
			if(!ComUtils.isEmptyString(bundle.getCrackedDataFile()))
				gtfsFile = new File(bundle.getCrackedDataFile());
			else
				gtfsFile = new File(bundle.getCurrentDataFile());
			GtfsUtils utils = new GtfsUtils(logger, loggerName);

			//read agency ids
			List<String> lstAgencyIds = utils.readAgencyIds(gtfsFile);
			bundle.setAgencyIds(lstAgencyIds);

			//read and calculate calander.txt
			try {
				List<GtfsCalander> lstCalanders = utils.readCalanderGtfs(gtfsFile);
				bundle.setLstCalanders(lstCalanders);
				for (GtfsCalander gtfsCalander : lstCalanders) {
					int[] weekServiceStatus = gtfsCalander.getWeeklyStatusForService();// all weekday status for particular service
					String[] serviceOnDays = bundle.getServiceOnDays();

					for (int i = 0; i < weekServiceStatus.length; i++) {
						if(weekServiceStatus[i] == 1){ // available
							if(ComUtils.isEmptyString(serviceOnDays[i]))
								serviceOnDays[i]= gtfsCalander.getServiceName();
							else
								serviceOnDays[i]= serviceOnDays[i]+","+gtfsCalander.getServiceName();
						}
					}
				}
			} catch (Exception e) {
				logger.error(loggerName, e);
			}
			sortAndHashServices();

			//read and calculate calander_dates.txt
			List<GtfsCalandeDates> lstCalanderDates = utils.readCalanderDatesGtfs(gtfsFile);
			//<date, service list>
			Map<String, List<String>> tempDateException = new HashMap<String, List<String>>();
			String[] serviceOnDays = bundle.getServiceOnDays();
			for (GtfsCalandeDates calDates : lstCalanderDates) {
				String strDates =  calDates.getDate();
				List<String> servicesOnDate = tempDateException.get(strDates);
				if(servicesOnDate==null){
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(gtfsDateFormat.parse(strDates));
					int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
					String service = serviceOnDays[dayOfWeek-1];
					servicesOnDate = ComUtils.isEmptyString(service)? new ArrayList<String>():ComUtils.getListFromArray(service.split(","));
					tempDateException.put(strDates, servicesOnDate);
				}
				if(calDates.getServiceType().equals("1")){ // added
					servicesOnDate.add(calDates.getServiceName());
				}else if(calDates.getServiceType().equals("2")){ //removed
					if(!servicesOnDate.contains(calDates.getServiceName()))
						logger.warn(loggerName, "No service name found in calander.txt to remove according to calander_dates.txt for :"+
								bundle.getDefaultAgencyId()+", service name: "+calDates.getServiceName()+", on date: "+calDates.getDate());
					servicesOnDate.remove(calDates.getServiceName());
				}else{
					logger.warn(loggerName, "Invalid service exception type found in calander_dates.txt for :"+
							bundle.getDefaultAgencyId()+", service name: "+calDates.getServiceName()+", on date:" +
							calDates.getDate()+",service type:"+calDates.getServiceType());
				}
			}
			// sort
			Map<String, String> datesAndServiceWithException =  new HashMap<String, String>();
			for (Map.Entry<String, List<java.lang.String>> entry : tempDateException.entrySet()) {
				String key = entry.getKey();
				List<String> value = entry.getValue();
				Collections.sort(value);
				datesAndServiceWithException.put(key, StringUtils.join(value, ","));
			}
			bundle.setDatesAndServiceWithException(datesAndServiceWithException);
			bundle.setExtracted(true);
			System.out.println(String.format("Gtfs file read complete for    :      [%-15s]", getBundle().getDefaultAgencyId()));
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
	}

	/**
	 * Sort services.
	 */
	private void sortAndHashServices() {
		String[] serviceOnDays = bundle.getServiceOnDays();
		String[] hash = bundle.getServiceOnDaysHash();
		for (int i = 0; i < serviceOnDays.length; i++) {
			if(ComUtils.isEmptyString(serviceOnDays[i]))
				continue;
			String[] temp = serviceOnDays[i].split(",");
			Arrays.sort(temp);
			serviceOnDays[i] = StringUtils.join(temp,",");
			//hash
			if(serviceOnDays[i].length()>MIN_SIZE_TO_SORT)
				hash[i] = hash(serviceOnDays[i]);
			else
				hash[i] = serviceOnDays[i];
		}
	}

	public GtfsBundle getBundle() {
		return bundle;
	}

	public void setBundle(GtfsBundle bundle) {
		this.bundle = bundle;
	}


	public static void main(String[] args) {
		try {
			GtfsBundle bundle = new GtfsBundle();
			bundle.setCurrentDataFile("C:/OTP_/GTFS/BART.zip");
			GtfsMetaExtractor extractor = new GtfsMetaExtractor(bundle,null,null);
			extractor.setBundle(bundle);
			extractor.run();
			System.out.println(extractor.getBundle().getLstCalanders());
			System.out.println(extractor.getBundle().getLstCalandeDates());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Hash.
	 *
	 * @param text the text
	 * @return the string
	 */
	public String hash(String text) {
		try {
			if(bundle.isEnableHashing()){
				byte[] bytesOfMessage = text.getBytes("UTF-8");
				String hash= DigestUtils.md5DigestAsHex(bytesOfMessage);
				return hash;
			}
		} catch (Exception e) {
			logger.error(loggerName, "error while hash: rerurning input",e);
		}
		return text;
	}

}
