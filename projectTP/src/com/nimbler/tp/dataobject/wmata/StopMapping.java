package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.BidiMap;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * The Class StopMapping.
 * @author nirmal
 */
public class StopMapping implements Serializable{

	public enum  LoadLevel{
		BASIC,
		DEBUG
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 6917209651167560128L;

	/**
	 * Maps Gtfs stop id to {@link GtfsStop}
	 */
	private Map<String, GtfsStop> busGtfsStopsById;
	/**
	 * Maps Wmata api stop id to {@link BusStop}
	 */
	private Map<String, BusStop> busApiStopsById;
	/**
	 * Maps gtfs routes id to multiple API route id<br />
	 * <b>&lt; 23C --> [ 23Cv1, 23Cv2 ] &gt;</b>
	 */
	private Multimap<String, String> busRouteIdMapping;

	/**
	 * <b>&lt; gtfs route short name -- gtfs trip headsign -- direction id &gt; </b><br />
	 * i.e &lt; 26C -- RANDOLPH RD & PUTNAM RD -- 1 &gt;
	 */
	private Table<String, String, String> busApiHeadSign;

	/* =============================== rail ====================*/
	/**
	 * @return <Gtfs Stop ID,GtfsStop>
	 */
	private Map<String, GtfsStop> railGtfsStopsById;

	private Map<RailLine, List<RailStation>> railStationByRailLine;

	/**
	 * Maps code to name<br / >
	 * <b>RD --> red (lowercase)<b/>
	 */
	private BidiMap railLineCode;



	/**
	 * <Trip,Last gtfs stop id>
	 */
	private Map<String, String> railTripLastStop;
	/**
	 * rail api stop sequence by line code. e.g RD
	 */
	private Map<String, RailStopSequence> railStopSequence;

	private transient Map<String, RailStation>  railApiStops;

	private String version;
	private String note;
	private transient String log = "";
	private Long date;


	public StopMapping() {
	}

	public List<RailStation> getAllRailStations() {
		List<RailStation> res = null;
		if(railStationByRailLine!=null){
			res = new ArrayList<RailStation>();
			Collection<List<RailStation>> lists =railStationByRailLine.values();
			for (List<RailStation> list : lists) {
				res.addAll(list);
			}
		}
		return res;
	}

	/**
	 * Gets the rail api stop sequence by line code.
	 *
	 * @return the railStopSequence
	 */
	public Map<String, RailStopSequence> getRailStopSequence() {
		return railStopSequence;
	}

	/**
	 * @param railStopSequence the railStopSequence to set
	 */
	public void setRailStopSequence(Map<String, RailStopSequence> railStopSequence) {
		this.railStopSequence = railStopSequence;
	}

	/**
	 * Gets the maps code to name<br / > <b>RD --> red (lowercase)<b/>.
	 *
	 * @return the maps code to name<br / > <b>RD --> red (lowercase)<b/>
	 */
	public BidiMap getRailLineCode() {
		return railLineCode;
	}
	public String getRailLineCodeByName(String name) {
		return (String) railLineCode.getKey(name.toLowerCase());
	}


	/**
	 * @return the log
	 */
	public String getLog() {
		return log;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(String log) {
		this.log = log;
	}

	/**
	 * Sets the maps code to name<br / > <b>RD --> Red<b/>.
	 *
	 * @param railLineCode the railLineCode to set
	 */
	public void setRailLineCode(BidiMap railLineCode) {
		this.railLineCode = railLineCode;
	}


	/**
	 * @return the railApiStops
	 */
	public synchronized Map<String, RailStation> getRailApiStops() {
		if(railApiStops==null && railStationByRailLine!=null){
			Map<String, RailStation> temp = new HashMap<String, RailStation>();
			for (Entry<RailLine, List<RailStation>> entry : railStationByRailLine.entrySet()) {
				List<RailStation> value = entry.getValue();
				for (RailStation railStation : value) {
					temp.put(railStation.getCode(), railStation);
				}
			}
			railApiStops = temp;
		}
		return railApiStops;
	}
	public void clearCache(){
		railApiStops = null;
	}

	/**
	 * Gets the maps Gtfs stop id to {@link GtfsStop}.
	 *
	 * @return the busGtfsStopsById
	 */
	public Map<String, GtfsStop> getBusGtfsStopsById() {
		return busGtfsStopsById;
	}
	/**
	 * Gets the maps Gtfs stop id to {@link GtfsStop}.
	 *
	 * @return the busGtfsStopsById
	 */
	public GtfsStop getGtfsBusStopById(String id) {
		return busGtfsStopsById.get(id);
	}
	/**
	 * Sets the maps Gtfs stop id to {@link GtfsStop}.
	 *
	 * @param busGtfsStopsById the busGtfsStopsById to set
	 */
	public void setBusGtfsStopsById(Map<String, GtfsStop> busGtfsStopsById) {
		this.busGtfsStopsById = busGtfsStopsById;
	}

	/**
	 * @return the busApiStopsById
	 */
	public Map<String, BusStop> getBusApiStopsById() {
		return busApiStopsById;
	}

	/**
	 * Sets the maps Wmata api stop id to {@link BusStop}.
	 *
	 * @param busApiStopsById the busApiStopsById to set
	 */
	public void setBusApiStopsById(Map<String, BusStop> busApiStopsById) {
		this.busApiStopsById = busApiStopsById;
	}

	/**
	 * Gets the maps gtfs routes id to multiple API route id<br /> <b>&lt; 23C --> [ 23Cv1, 23Cv2 ] &gt;</b>.
	 *
	 * @return the busRouteIdMapping
	 */
	public Multimap<String, String> getBusRouteIdMapping() {
		return busRouteIdMapping;
	}

	/**
	 * Sets the maps gtfs routes id to multiple API route id<br /> <b>&lt; 23C --> [ 23Cv1, 23Cv2 ] &gt;</b>.
	 *
	 * @param busRouteIdMapping the busRouteIdMapping to set
	 */
	public void setBusRouteIdMapping(Multimap<String, String> busRouteIdMapping) {
		this.busRouteIdMapping = busRouteIdMapping;
	}

	/**
	 * Gets the <b>&lt; route short name -- gtfs trip headsign -- direction id &gt; </b><br /> i.
	 *
	 * @return the busApiHeadSign
	 */
	public Table<String, String, String> getBusApiHeadSign() {
		return busApiHeadSign;
	}

	/**
	 * Sets the <b>&lt; route short name -- gtfs trip headsign -- direction id &gt; </b><br /> i.
	 *
	 * @param busApiHeadSign the busApiHeadSign to set
	 */
	public void setBusApiHeadSign(Table<String, String, String> busApiHeadSign) {
		this.busApiHeadSign = busApiHeadSign;
	}

	/**
	 * Gets the rail gtfs stops by id.
	 *
	 * @return the railGtfsStopsById
	 */
	public Map<String, GtfsStop> getRailGtfsStopsById() {
		return railGtfsStopsById;
	}

	/**
	 * @param railGtfsStopsById the railGtfsStopsById to set
	 */
	public void setRailGtfsStopsById(Map<String, GtfsStop> railGtfsStopsById) {
		this.railGtfsStopsById = railGtfsStopsById;
	}

	/**
	 * Gets the rail station by rail line.
	 *
	 * @return the railStationByRailLine
	 */
	public Map<RailLine, List<RailStation>> getRailStationByRailLine() {
		return railStationByRailLine;
	}

	/**
	 * Sets the rail station by rail line.
	 *
	 * @param railStationByRailLine the railStationByRailLine to set
	 */
	public void setRailStationByRailLine(
			Map<RailLine, List<RailStation>> railStationByRailLine) {
		this.railStationByRailLine = railStationByRailLine;
	}


	/**
	 * @return the railTripLastStop
	 */
	public Map<String, String> getRailTripLastStop() {
		return railTripLastStop;
	}

	/**
	 * @param railTripLastStop the railTripLastStop to set
	 */
	public void setRailTripLastStop(Map<String, String> railTripLastStop) {
		this.railTripLastStop = railTripLastStop;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StopMapping ["
				+ (version != null ? "version=" + version + ", " : "")
				+ (date != null ? "date=" + new Date(date) + ", " : "")
				+ (note != null ? "note=" + note + ", " : "")
				+ (busGtfsStopsById != null ? "busGtfsStopsById="
						+ busGtfsStopsById.size() + ", " : "")
						+ (busApiStopsById != null ? "busApiStopsById="
								+ busApiStopsById.size() + ", " : "")
								+ (busRouteIdMapping != null ? "busRouteIdMapping="
										+ busRouteIdMapping.size() + ", " : "")
										+ (busApiHeadSign != null ? "busApiHeadSign=" + busApiHeadSign.size()
												+ ", " : "")
												+ (railGtfsStopsById != null ? "railGtfsStopsById="
														+ railGtfsStopsById.size() + ", " : "")
														+ (railStationByRailLine != null ? "railStationByRailLine="
																+ railStationByRailLine.size() + ", " : "")
																+ (railLineCode != null ? "railLineCode=" + railLineCode.size() + ", "
																		: "")
																		+ (railTripLastStop != null ? "railTripLastStop="
																				+ railTripLastStop.size() : "") + "]";
	}



}
